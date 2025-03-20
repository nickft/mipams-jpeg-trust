package org.mipams.jpegtrust.builders;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustManifestContentType;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;

public class ManifestBuilder {

    private String uuid;
    private JumbfBoxBuilder assertionStoreJumbfBoxBuilder;
    private Claim claim;
    private byte[] claimSignature;
    private List<X509Certificate> claimSignatureCertificates;
    private TrustManifestContentType trustManifestContentType;
    private LinkedHashSet<JumbfBox> gatheredAssertions;
    private LinkedHashSet<JumbfBox> createdAssertions;

    public ManifestBuilder(TrustManifestContentType trustManifestContentType) {
        this.trustManifestContentType = trustManifestContentType;
        this.uuid = issueNewManifestId();
        this.claim = new Claim();
        this.claim.setSignature(String.format("self#jumbf=c2pa.signature", this.uuid));
        this.claimSignature = new byte[0];
        this.gatheredAssertions = new LinkedHashSet<>();
        this.createdAssertions = new LinkedHashSet<>();
    }

    public ManifestBuilder addIngredientAssertion(IngredientAssertionV1 ingredientAssertion,
            JumbfBox ingredientManifest) throws MipamsException {
        if (ingredientManifest.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(
                    String.format("Ingredient manifest with no label %s", ingredientManifest.toString()));
        }
        final byte[] locallyComputedHash = JpegTrustUtils.calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference
                .setUrl(String.format("self#jumbf=/c2pa/%s", ingredientManifest.getDescriptionBox().getLabel()));
        hashedUriReference.setDigest(locallyComputedHash);

        ingredientAssertion.setManifestReference(hashedUriReference);

        addCreatedAssertion(ingredientAssertion);

        return this;
    }

    public ManifestBuilder addIngredientAssertion(IngredientAssertion ingredientAssertion, JumbfBox ingredientManifest)
            throws MipamsException {
        if (ingredientManifest.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(
                    String.format("Ingredient manifest with no label %s", ingredientManifest.toString()));
        }
        final byte[] locallyComputedHash = JpegTrustUtils.calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference
                .setUrl(String.format("self#jumbf=/c2pa/%s", ingredientManifest.getDescriptionBox().getLabel()));
        hashedUriReference.setDigest(locallyComputedHash);

        ingredientAssertion.setActiveManifestOfIngredient(hashedUriReference);

        addCreatedAssertion(ingredientAssertion);

        return this;
    }

    public ManifestBuilder addGatheredAssertion(Assertion assertion) throws MipamsException {
        JumbfBox assertionJumbfBox = assertion.toJumbfBox();

        if (assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        gatheredAssertions.add(assertionJumbfBox);

        return this;
    }

    public ManifestBuilder addCreatedAssertion(Assertion assertion) throws MipamsException {
        JumbfBox assertionJumbfBox = assertion.toJumbfBox();

        if (assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        createdAssertions.add(assertionJumbfBox);

        return this;
    }

    public ManifestBuilder addRedactedAssertion(String jumbfUriReference) throws MipamsException {
        if (claim.getRedactedAssertions() == null) {
            claim.setRedactedAssertions(new LinkedHashSet<>());
        }

        return this;
    }

    public ManifestBuilder setInstanceID(String instanceID) throws MipamsException {
        this.claim.setInstanceId(instanceID);
        return this;
    }

    public ManifestBuilder setTitle(String title) throws MipamsException {
        this.claim.setTitle(title);
        return this;
    }

    public ManifestBuilder setGeneratorInfoName(String generatorInfoName) throws MipamsException {
        this.claim.getClaimGeneratorInfo().put("name", generatorInfoName);
        return this;
    }

    public byte[] encodeClaimToBeSigned() throws MipamsException {
        try {
            ensureLabelUniquenessInAssertionStore();
            buildClaimAssertions();
            return CoseUtils.toSigStructure(claim, claimSignatureCertificates);
        } catch (Exception e) {
            throw new MipamsException(e);
        }
    }

    private void buildClaimAssertions() throws MipamsException {
        this.claim.getCreatedAssertions().clear();
        this.claim.getGatheredAssertions().clear();

        for (JumbfBox assertionJumbfBox : this.createdAssertions) {
            final byte[] locallyComputedHash = JpegTrustUtils.calculateDigestForJumbfBox(assertionJumbfBox);

            final HashedUriReference hashedUriReference = new HashedUriReference();
            hashedUriReference.setUrl(
                    String.format("self#jumbf=c2pa.assertions/%s", assertionJumbfBox.getDescriptionBox().getLabel()));
            hashedUriReference.setDigest(locallyComputedHash);

            this.claim.getCreatedAssertions().add(hashedUriReference);
        }

        for (JumbfBox assertionJumbfBox : this.gatheredAssertions) {
            final byte[] locallyComputedHash = JpegTrustUtils.calculateDigestForJumbfBox(assertionJumbfBox);

            final HashedUriReference hashedUriReference = new HashedUriReference();
            hashedUriReference.setUrl(
                    String.format("self#jumbf=c2pa.assertions/%s", assertionJumbfBox.getDescriptionBox().getLabel()));
            hashedUriReference.setDigest(locallyComputedHash);

            this.claim.getGatheredAssertions().add(hashedUriReference);
        }
    }

    public ManifestBuilder setClaimSignature(byte[] coseEncodedByteArray) throws MipamsException {
        this.claimSignature = coseEncodedByteArray;
        return this;
    }

    public ManifestBuilder setClaimSignatureCertificates(List<X509Certificate> certificates) throws MipamsException {
        this.claimSignatureCertificates = new ArrayList<>(certificates);
        return this;
    }

    public JumbfBox build() throws MipamsException {
        if (gatheredAssertions.isEmpty() && createdAssertions.isEmpty()) {
            throw new MipamsException("Empty manifest: No assertions specified");
        }

        List<JumbfBox> assertions = new ArrayList<>();
        assertions.addAll(createdAssertions);
        assertions.addAll(gatheredAssertions);

        final AssertionStoreContentType assertionStoreContentType = new AssertionStoreContentType();
        assertionStoreJumbfBoxBuilder = new JumbfBoxBuilder(assertionStoreContentType);
        assertionStoreJumbfBoxBuilder.setJumbfBoxAsRequestable();
        assertionStoreJumbfBoxBuilder.setLabel(assertionStoreContentType.getLabel());
        assertionStoreJumbfBoxBuilder.appendAllContentBoxes(assertions);

        JumbfBoxBuilder manifestJumbfBoxBuilder = new JumbfBoxBuilder(trustManifestContentType);
        manifestJumbfBoxBuilder.setJumbfBoxAsRequestable();
        manifestJumbfBoxBuilder.setLabel(this.uuid);
        manifestJumbfBoxBuilder.appendContentBox(assertionStoreJumbfBoxBuilder.getResult());
        manifestJumbfBoxBuilder.appendContentBox(getClaimJumbfBox());
        manifestJumbfBoxBuilder.appendContentBox(getClaimSignatureJumbfBox());
        return manifestJumbfBoxBuilder.getResult();
    }

    private JumbfBox getClaimJumbfBox() throws MipamsException {
        final ClaimContentType claimContentType = new ClaimContentType();

        final JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(claimContentType);
        jumbfBoxBuilder.setJumbfBoxAsRequestable();
        jumbfBoxBuilder.setLabel(claimContentType.getLabel());

        CborBox cborBox = new CborBox();
        cborBox.setContent(CoseUtils.toCborEncodedByteArray(this.claim));

        jumbfBoxBuilder.appendContentBox(cborBox);

        return jumbfBoxBuilder.getResult();
    }

    private static String issueNewManifestId() {
        return String.format("urn:c2pa:%s", CoreUtils.randomStringGenerator());
    }

    private JumbfBox getClaimSignatureJumbfBox() throws MipamsException {
        final ClaimSignatureContentType claimSignatureContentType = new ClaimSignatureContentType();

        final JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(claimSignatureContentType);
        jumbfBoxBuilder.setJumbfBoxAsRequestable();
        jumbfBoxBuilder.setLabel(claimSignatureContentType.getLabel());

        final byte[] coseSign1 = CoseUtils.toCoseSign1EncodedBytestream(this.claimSignatureCertificates,
                this.claimSignature);

        CborBox cborBox = new CborBox();
        cborBox.setContent(coseSign1);
        jumbfBoxBuilder.appendContentBox(cborBox);

        return jumbfBoxBuilder.getResult();
    }

    public void removeCreatedAssertion(String label) {
        this.claim.getCreatedAssertions().removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.createdAssertions = new LinkedHashSet<>(this.createdAssertions.stream()
                .filter(entrySet -> !entrySet.getDescriptionBox().getLabel().equals(label))
                .collect(Collectors.toSet()));

    }

    public void removeGatheredAssertion(String label) {
        this.claim.getGatheredAssertions().removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.gatheredAssertions = new LinkedHashSet<>(this.gatheredAssertions.stream()
                .filter(entrySet -> !entrySet.getDescriptionBox().getLabel().equals(label))
                .collect(Collectors.toSet()));

    }

    private void ensureLabelUniquenessInAssertionStore() throws MipamsException {
        Map<String, Long> labelOccurenceMap = computeDuplicateLabelOccurrenceMap();

        LinkedHashSet<JumbfBox> gatheredResullt = new LinkedHashSet<>();
        for (JumbfBox assertion : this.gatheredAssertions) {
            final String commonLabel = assertion.getDescriptionBox().getLabel();
            Long occurences = labelOccurenceMap.get(commonLabel);

            if (occurences == null) {
                gatheredResullt.add(assertion);
                continue;
            }

            String uniqueLabel = String.format("%s__%d", commonLabel, occurences);
            JumbfBoxBuilder builder = new JumbfBoxBuilder(assertion);
            builder.setLabel(uniqueLabel);

            gatheredResullt.add(builder.getResult());

            labelOccurenceMap.put(commonLabel, --occurences);
        }
        this.gatheredAssertions = gatheredResullt;

        LinkedHashSet<JumbfBox> createdResullt = new LinkedHashSet<>();
        for (JumbfBox assertion : this.createdAssertions) {
            final String commonLabel = assertion.getDescriptionBox().getLabel();
            Long occurences = labelOccurenceMap.get(commonLabel);

            if (occurences == null) {
                createdResullt.add(assertion);
                continue;
            }

            String uniqueLabel = String.format("%s__%d", commonLabel, occurences);
            JumbfBoxBuilder builder = new JumbfBoxBuilder(assertion);
            builder.setLabel(uniqueLabel);

            createdResullt.add(builder.getResult());

            labelOccurenceMap.put(commonLabel, --occurences);
        }
        this.createdAssertions = createdResullt;
    }

    private Map<String, Long> computeDuplicateLabelOccurrenceMap() throws MipamsException {

        List<JumbfBox> allAssertions = new ArrayList<>();
        allAssertions.addAll(this.gatheredAssertions);
        allAssertions.addAll(this.createdAssertions);

        return JpegTrustUtils.computeDuplicateLabelOccurrenceMap(allAssertions);
    }
}
