package org.mipams.jpegtrust.services.validation.consumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery.MipamsAssertion;
import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssertionConsumer {

    private static final Logger logger = Logger.getLogger(AssertionConsumer.class.getName());

    @Autowired
    AssertionStoreContentType assertionStoreContentType;

    @Autowired
    AssertionDiscovery assertionDiscovery;

    @Autowired
    CoreGeneratorService coreGeneratorService;

    public void validateContentBinding(JumbfBox manifestJumbfBox, String assetUrl) throws ValidationException {

        BindingAssertion assertion;
        try {
            JumbfBox assertionStoreJumbfBox = getAssertionStoreJumbfBox(manifestJumbfBox);

            JumbfBox contentBindingAssertionJumbfBox = getContentBindingAssertionJumbfBox(assertionStoreJumbfBox);

            assertion = deserializeBindingAssertion(contentBindingAssertionJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (!assertion.getAlgorithm().equals("sha256")) {
            throw new ValidationException(ValidationCode.ALGORITHM_UNSUPPORTED);
        }

        try {
            byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetUrl, assertion.getExclusions());
            logger.log(Level.FINE, String.format("Checking hash digest for asset: %s", assetUrl));

            if (!Arrays.equals(assertion.getDigest(), digest)) {
                throw new ValidationException(ValidationCode.ASSERTION_DATA_HASH_MISMATCH);
            }
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.ASSERTION_DATA_HASH_MISMATCH);
        }
    }

    private JumbfBox getAssertionStoreJumbfBox(JumbfBox manifestJumbfBox) throws MipamsException {
        for (BmffBox contentBox : manifestJumbfBox.getContentBoxList()) {

            JumbfBox jumbfBox = (JumbfBox) contentBox;

            if (assertionStoreContentType.getLabel().equals(jumbfBox.getDescriptionBox().getLabel())) {
                return jumbfBox;
            }
        }
        throw new MipamsException(ValidationCode.ASSERTION_INACCESSIBLE.getCode());
    }

    private JumbfBox getContentBindingAssertionJumbfBox(JumbfBox assertionStoreJumbfBox) throws MipamsException {
        for (BmffBox contentBox : assertionStoreJumbfBox.getContentBoxList()) {

            JumbfBox jumbfBox = (JumbfBox) contentBox;

            if (assertionDiscovery.labelReferencesContentBindingAssertion(jumbfBox.getDescriptionBox().getLabel())) {
                return jumbfBox;
            }
        }
        throw new MipamsException(ValidationCode.ASSERTION_INACCESSIBLE.getCode());
    }

    private BindingAssertion deserializeBindingAssertion(JumbfBox contentBindingAssertionJumbfBox)
            throws MipamsException {

        CborBox contentBindingCborBox = (CborBox) contentBindingAssertionJumbfBox.getContentBoxList().get(0);
        ObjectMapper mapper = new CBORMapper();
        try {
            return mapper.readValue(new ByteArrayInputStream(contentBindingCborBox.getContent()),
                    BindingAssertion.class);
        } catch (IOException e) {
            throw new MipamsException(ValidationCode.ASSERTION_DATA_HASH_MALFORMED.getCode(), e);
        }
    }

    public Map<String, String> validateAssertionsIntegrityAndGetAssertionStatus(String manifestId,
            LinkedHashSet<HashedUriReference> assertionReferenceList, JumbfBox manifestStoreJumbfBox)
            throws ValidationException {

        List<HashedUriReference> computedUriReferenceList;
        try {
            JumbfBox manifestJumbfBox = JpegTrustUtils.locateManifestFromUri(manifestStoreJumbfBox, manifestId).get();
            JumbfBox assertionStoreJumbfBox = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(manifestJumbfBox,
                    assertionStoreContentType);

            computedUriReferenceList = getAssertionReferenceListFromAssertionStore(manifestId, assertionStoreJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        Map<String, HashedUriReference> uriToReferenceMap = new HashMap<>();
        String absolutePrefix = JpegTrustUtils.getProvenanceJumbfURL(manifestId);
        assertionReferenceList.forEach(assertionRef -> uriToReferenceMap
                .put(assertionRef.getUrl().replace("self#jumbf=", absolutePrefix + "/"), assertionRef));

        HashMap<String, String> validationReport = new HashMap<>();

        for (HashedUriReference computedUriReference : computedUriReferenceList) {

            HashedUriReference claimedUriReference = uriToReferenceMap.get(computedUriReference.getUrl());

            if (claimedUriReference == null) {
                throw new ValidationException(ValidationCode.ASSERTION_INACCESSIBLE);
            }

            if (Arrays.equals(computedUriReference.getDigest(), claimedUriReference.getDigest())) {
                validationReport.put(JpegTrustUtils.getLabelFromManifestUri(absolutePrefix),
                        ValidationCode.ASSERTION_HASHED_URI_MATCH.getCode());
            } else {
                validationReport.put(JpegTrustUtils.getLabelFromManifestUri(absolutePrefix),
                        ValidationCode.HASHED_URI_MISMATCH.getCode());
            }
        }

        return validationReport;
    }

    public List<HashedUriReference> getAssertionReferenceListFromAssertionStore(String manifestId,
            JumbfBox assertionStore) throws MipamsException {

        List<HashedUriReference> result = new ArrayList<>();

        for (BmffBox contentBox : assertionStore.getContentBoxList()) {

            JumbfBox jumbfBox = (JumbfBox) contentBox;

            byte[] digest = JpegTrustUtils.calculateDigestForJumbfBox(jumbfBox);
            String uri = computeUriForAssertion(manifestId, assertionStore, jumbfBox);
            HashedUriReference ref = new HashedUriReference(digest, uri, HashedUriReference.SUPPORTED_HASH_ALGORITHM);
            result.add(ref);
        }
        return result;
    }

    private String computeUriForAssertion(String manifestId, JumbfBox assertionStore, JumbfBox jumbfBox) {
        String assertionStoreLabel = assertionStore.getDescriptionBox().getLabel();
        String assertionLabel = jumbfBox.getDescriptionBox().getLabel();

        return JpegTrustUtils.getProvenanceJumbfURL(manifestId, assertionStoreLabel, assertionLabel);
    }

    public boolean containsActionAssertion(JumbfBox manifestJumbfBox) throws MipamsException {
        return getActionAssertions(manifestJumbfBox).size() > 0;
    }

    public boolean containsMultipleHardBindingToContentAssertions(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.CONTENT_BINDING).size() > 1;
    }

    public boolean containsHardBindingToContentAssertion(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.CONTENT_BINDING).size() > 0;
    }

    public boolean containsMultipleIngredientAssertions(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.INGREDIENT, MipamsAssertion.INGREDIENT_V2,
                MipamsAssertion.INGREDIENT_V1).size() > 1;
    }

    public boolean containsIngredientsOfRelationshipParentOf(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectIngredientAssertionsOfRelationship(manifestJumbfBox, IngredientAssertionV1.RELATIONSHIP_PARENT_OF)
                .size() > 0;
    }

    public Optional<Assertion> getIngredientAssertion(JumbfBox manifestJumbfBox) {
        try {
            return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.INGREDIENT,
                    MipamsAssertion.INGREDIENT_V2, MipamsAssertion.INGREDIENT_V1).stream().findAny();
        } catch (MipamsException e) {
            return Optional.empty();
        }
    }

    public List<Assertion> getActionAssertions(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.ACTION, MipamsAssertion.ACTION_V1);
    }

    private List<Assertion> collectAssertionsBasedOnType(JumbfBox manifestJumbfBox, MipamsAssertion... assertionTypes)
            throws MipamsException {
        JumbfBox assertionStore = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(manifestJumbfBox,
                assertionStoreContentType);

        HashSet<MipamsAssertion> allowedAssertionTypes = new HashSet<>(Set.of(assertionTypes));

        List<JumbfBox> result = assertionStore.getContentBoxList().stream().map(box -> (JumbfBox) box)
                .collect(Collectors.toList());

        List<Assertion> actionAssertions = new ArrayList<>();

        for (JumbfBox assertionBox : result) {
            Assertion assertion = assertionDiscovery.convertJumbfBoxToAssertion(assertionBox);
            if (assertion == null) {
                continue;
            }

            MipamsAssertion assertionType = MipamsAssertion.getTypeFromLabel(assertion.getLabel());

            if (!allowedAssertionTypes.contains(assertionType)) {
                continue;
            }
            actionAssertions.add(assertion);
        }
        return actionAssertions;
    }

    public List<Assertion> collectIngredientAssertionsOfRelationship(JumbfBox manifestJumbfBox, String relationship)
            throws MipamsException {
        List<Assertion> ingredientAssertions = collectAssertionsBasedOnType(manifestJumbfBox,
                MipamsAssertion.INGREDIENT, MipamsAssertion.INGREDIENT_V2, MipamsAssertion.INGREDIENT_V1);

        List<Assertion> result = new ArrayList<>();

        for (Assertion assertion : ingredientAssertions) {
            if (!relationship.equals(assertionDiscovery.extractIngredientProfile(assertion))) {
                continue;
            }
            result.add(assertion);
        }

        return result;
    }

    public List<Assertion> collectIngredientAssertions(JumbfBox manifestJumbfBox) throws MipamsException {
        return collectAssertionsBasedOnType(manifestJumbfBox, MipamsAssertion.INGREDIENT, MipamsAssertion.INGREDIENT_V2,
                MipamsAssertion.INGREDIENT_V1);
    }
}
