package org.mipams.jpegtrust.services.validation.consumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.ProvenanceEntity;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimIndicators;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimIndicatorsInterface;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimSignatureIndicators;
import org.mipams.jpegtrust.entities.validation.trustindicators.ManifestIndicators;
import org.mipams.jpegtrust.jpeg_systems.JumbfUtils;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustManifestContentType;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.services.validation.discovery.ManifestDiscovery;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManifestConsumer {

    @Autowired
    ManifestDiscovery manifestDiscovery;

    @Autowired
    ClaimSignatureContentType claimSignatureContentType;

    @Autowired
    ClaimContentType claimContentType;

    @Autowired
    AssertionConsumer assertionConsumer;

    @Autowired
    AssertionDiscovery assertionDiscovery;

    @Autowired
    AssertionStoreContentType assertionStoreContentType;

    @Autowired
    ClaimSignatureConsumer claimSignatureConsumer;

    @Autowired
    ClaimConsumer claimConsumer;

    @Autowired
    CoreGeneratorService coreGeneratorService;

    public ManifestIndicators validateManifest(String manifestUuid, JumbfBox manifestStoreJumbfBox,
            HashSet<String> redactedAssertions) {

        final ManifestIndicators manifestIndicators = new ManifestIndicators();

        ClaimIndicatorsInterface claimIndicators;
        JumbfBox currentManifest;

        try {
            try {
                currentManifest = JpegTrustUtils.locateManifestOnManifestStore(manifestUuid, manifestStoreJumbfBox);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.MANIFEST_INACCESSIBLE);
            }

            JumbfBox claimJumbfBox;

            try {
                claimJumbfBox = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(currentManifest,
                        claimContentType);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.CLAIM_MISSING);
            }

            JumbfBox claimSignatureJumbfBox;
            try {
                claimSignatureJumbfBox = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(
                        currentManifest, claimSignatureContentType);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.CLAIM_SIGNATURE_MISSING);
            }

            try {
                ProvenanceEntity claimStructureToBeSigned = claimConsumer.deserializeClaimJumbfBox(claimJumbfBox);
                claimIndicators = claimConsumer.buildClaimIndicatorSet(claimStructureToBeSigned);
                ClaimSignatureIndicators claimSignatureIndicators = claimSignatureConsumer
                        .buildClaimSignatureIndicatorSet(claimSignatureJumbfBox);

                manifestIndicators.setClaim(claimIndicators);
                manifestIndicators.setSignature(claimSignatureIndicators);

                try {
                    claimSignatureConsumer.validateSignature(claimSignatureJumbfBox,
                            claimStructureToBeSigned);
                    claimIndicators.setSignatureStatus(ValidationCode.CLAIM_SIGNATURE_VALIDATED);
                    String claimSignatureUri = JpegTrustUtils.getProvenanceJumbfURL(manifestUuid,
                            claimSignatureContentType.getLabel());
                    claimIndicators.setSignature(claimSignatureUri);
                    manifestIndicators.setClaim(claimIndicators);
                } catch (MipamsException e) {
                    claimIndicators.setSignatureStatus(ValidationCode.CLAIM_SIGNATURE_MISMATCH);
                    manifestIndicators.setClaim(claimIndicators);
                    return manifestIndicators;
                }
            } catch (ValidationException e) {
                throw new ValidationException(e.getStatusCode(), e);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.CLAIM_CBOR_INVALID);
            }

            TrustManifestContentType contentType;
            try {
                contentType = manifestDiscovery.discoverManifestType(currentManifest);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.GENERAL_ERROR);
            }

            contentType.validateTypeOfAssertions(currentManifest);

            validateRedactedAssertionsInCurrentClaim(currentManifest);

            LinkedHashSet<HashedUriReference> referencedAssertions = claimConsumer
                    .getNonRedactedAssertionsFromClaimInManifestJumbfBox(currentManifest, redactedAssertions);

            claimIndicators.getAssertionStatus()
                    .putAll(assertionConsumer.validateAssertionsIntegrityAndGetAssertionStatus(manifestUuid,
                            referencedAssertions, manifestStoreJumbfBox));

            int numberOfVisitedAssertions = 0;

            for (HashedUriReference ref : referencedAssertions) {
                String absoluteAssertionJumbfUri = ref.getUrl();

                if (!JumbfUtils.isJumbfUriAbsolute(ref.getUrl())) {
                    absoluteAssertionJumbfUri = JpegTrustUtils.getProvenanceJumbfURL(
                            manifestUuid, JumbfUtils.extractJumbfFragmentFromUri(ref.getUrl()));
                }

                Optional<JumbfBox> assertionJumbfBox = JumbfUtils.searchJumbfBox(manifestStoreJumbfBox,
                        absoluteAssertionJumbfUri);

                if (assertionJumbfBox.isEmpty()) {
                    throw new ValidationException(ValidationCode.GENERAL_ERROR);
                }
                numberOfVisitedAssertions++;

                Assertion assertion = assertionDiscovery.convertJumbfBoxToAssertion(assertionJumbfBox.get());

                if (assertion == null) {
                    continue;
                }

                if (redactedAssertions.contains(absoluteAssertionJumbfUri) && !assertion.isReductable()) {
                    throw new ValidationException(ValidationCode.GENERAL_ERROR);
                }

                manifestIndicators.getAssertions()
                        .put(assertionJumbfBox.get().getDescriptionBox().getLabel(), assertion);
            }

            if (numberOfVisitedAssertions != extractAssertionsFromAssertionStore(currentManifest).size()) {
                throw new ValidationException(ValidationCode.ASSERTION_UNDECLARED);
            }

            return manifestIndicators;

        } catch (ValidationException e) {
            claimIndicators = new ClaimIndicators();
            claimIndicators.setContentStatus(e.getStatusCode());
            claimIndicators.getAssertionStatus().clear();
            manifestIndicators.getAssertions().clear();
            manifestIndicators.setClaim(claimIndicators);
            return manifestIndicators;
        } catch (MipamsException e) {
            claimIndicators = new ClaimIndicators();
            claimIndicators.setContentStatus(ValidationCode.GENERAL_ERROR);
            claimIndicators.getAssertionStatus().clear();
            manifestIndicators.getAssertions().clear();
            manifestIndicators.setClaim(claimIndicators);
            return manifestIndicators;
        }
    }

    public HashSet<String> extractRedactedAssertions(String manifestUuid, JumbfBox manifestStoreJumbfBox)
            throws MipamsException {
        final JumbfBox currentManifest = CoreUtils.locateJumbfBoxFromLabel(manifestStoreJumbfBox.getContentBoxList()
                .stream().map(box -> (JumbfBox) box).collect(Collectors.toList()), manifestUuid);
        return claimConsumer.extractRedactedAssertions(currentManifest);
    }

    private void validateRedactedAssertionsInCurrentClaim(JumbfBox manifest) throws ValidationException {
        try {
            claimConsumer.extractRedactedAssertions(manifest);
        } catch (ValidationException e) {
            throw new ValidationException(e.getStatusCode(), e);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR);
        }
    }

    public Map<String, HashedUriReference> getIngredientManifests(String manifestUuid,
            JumbfBox manifestStoreJumbfBox) throws MipamsException {
        final JumbfBox currentManifest = CoreUtils.locateJumbfBoxFromLabel(
                manifestStoreJumbfBox.getContentBoxList().stream()
                        .map(box -> (JumbfBox) box).collect(Collectors.toList()),
                manifestUuid);

        List<Assertion> ingredientAssertions = assertionConsumer.collectIngredientAssertions(currentManifest);

        Map<String, HashedUriReference> result = new HashMap<>();
        for (Assertion assertion : ingredientAssertions) {
            HashedUriReference ref = assertionDiscovery.extractTargetManifestFromIngredient(assertion);
            result.put(ref.getUrl(), ref);
        }

        return result;
    }

    private List<JumbfBox> extractAssertionsFromAssertionStore(JumbfBox currentManifest)
            throws MipamsException {
        JumbfBox assertionStoreJumbfBox = JpegTrustUtils
                .locateJpegTrustJumbfBoxByContentType(currentManifest, assertionStoreContentType);
        List<JumbfBox> result = assertionStoreJumbfBox.getContentBoxList().stream().map(box -> (JumbfBox) box)
                .collect(Collectors.toList());

        Map<String, Long> duplicateAssertionLabels = JpegTrustUtils.computeDuplicateLabelOccurrenceMap(result);
        if (!duplicateAssertionLabels.isEmpty()) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR);
        }

        return result;
    }

    public String locateManifestWithEnforceableContentBinding(JumbfBox manifestStoreJumbfBox)
            throws MipamsException {
        JumbfBox currentManifest = JpegTrustUtils.locateActiveManifest(manifestStoreJumbfBox);
        TrustManifestContentType contentType = manifestDiscovery.discoverManifestType(currentManifest);

        while (manifestDiscovery.isUpdateManifestRequest(contentType)) {
            String url = assertionDiscovery
                    .extractTargetManifestFromIngredient(assertionConsumer
                            .collectIngredientAssertions(currentManifest).getFirst())
                    .getUrl();
            Optional<JumbfBox> result = JumbfUtils.searchJumbfBox(manifestStoreJumbfBox, url);
            if (result.isEmpty()) {
                throw new ValidationException();
            }

            currentManifest = result.get();
            contentType = manifestDiscovery.discoverManifestType(currentManifest);
        }
        return currentManifest.getDescriptionBox().getLabel();
    }

    public void validateContentBinding(String manifestUuid, JumbfBox manifestStoreJumbfBox,
            String assetUrl) throws MipamsException {
        Optional<JumbfBox> manifest = JpegTrustUtils.locateManifestFromUri(manifestStoreJumbfBox, manifestUuid);

        if (manifest.isEmpty()) {
            throw new ValidationException();
        }

        assertionConsumer.validateContentBinding(manifest.get(), assetUrl);
    }
}
