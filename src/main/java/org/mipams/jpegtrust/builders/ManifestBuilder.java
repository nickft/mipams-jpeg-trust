package org.mipams.jpegtrust.builders;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.DigestResultForJumbfBox;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
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
    private LinkedHashMap<JumbfBox, HashedUriReference> gatheredAssertions;
    private LinkedHashMap<JumbfBox, HashedUriReference> createdAssertions;

    public ManifestBuilder(TrustManifestContentType trustManifestContentType) {
        this.trustManifestContentType = trustManifestContentType;
        this.uuid = issueNewManifestId();
        this.claim = new Claim();
        this.claim.setSignature("self#jumbf=c2pa.signature");
        this.claimSignature = new byte[0];
        this.gatheredAssertions = new LinkedHashMap<>();
        this.createdAssertions = new LinkedHashMap<>();
    }

    public ManifestBuilder addGatheredAssertion(JumbfBox assertionJumbfBox, DigestResultForJumbfBox digest)
            throws MipamsException {

        if (assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference.setAlgorithm(digest.getAlgorithm());
        hashedUriReference.setDigest(digest.getDigest());
        hashedUriReference.setUrl(
                String.format("self#jumbf=c2pa.assertions/%s", assertionJumbfBox.getDescriptionBox().getLabel()));

        this.gatheredAssertions.put(assertionJumbfBox, hashedUriReference);

        return this;
    }

    public ManifestBuilder addCreatedAssertion(JumbfBox assertionJumbfBox, DigestResultForJumbfBox digest)
            throws MipamsException {

        if (assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        HashedUriReference hashedUriReference = new HashedUriReference();
        // hashedUriReference.setAlgorithm(digest.getAlgorithm());
        hashedUriReference.setDigest(digest.getDigest());
        hashedUriReference.setUrl(
                String.format("self#jumbf=c2pa.assertions/%s", assertionJumbfBox.getDescriptionBox().getLabel()));

        this.createdAssertions.put(assertionJumbfBox, hashedUriReference);

        return this;
    }

    public ManifestBuilder addRedactedAssertion(String jumbfUriReference) throws MipamsException {
        if (claim.getRedactedAssertions() == null) {
            claim.setRedactedAssertions(new LinkedHashSet<>());
        }

        claim.getRedactedAssertions().add(jumbfUriReference);

        return this;
    }

    public ManifestBuilder addProtectedAssertion(JumbfBox protectedAssertion, DigestResultForJumbfBox digest)
            throws MipamsException {
        if (protectedAssertion.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", protectedAssertion.toString()));
        }

        HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference.setAlgorithm(digest.getAlgorithm());
        hashedUriReference.setDigest(digest.getDigest());
        hashedUriReference.setUrl(
                String.format("self#jumbf=c2pa.assertions/%s", protectedAssertion.getDescriptionBox().getLabel()));

        this.createdAssertions.put(protectedAssertion, hashedUriReference);
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

    public ManifestBuilder setAlgorithm(String hashingAlgorithm) throws MipamsException {
        this.claim.setAlgorithm(hashingAlgorithm);
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

        this.createdAssertions.values().forEach(val -> this.claim.getCreatedAssertions().add(val));
        this.gatheredAssertions.values().forEach(val -> this.claim.getCreatedAssertions().add(val));
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

        ensureLabelUniquenessInAssertionStore();
        buildClaimAssertions();

        List<JumbfBox> assertions = new ArrayList<>();
        assertions.addAll(createdAssertions.keySet());
        assertions.addAll(gatheredAssertions.keySet());

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
        this.claim.getCreatedAssertions()
                .removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.createdAssertions.entrySet().removeIf(
                entry -> entry.getKey()
                        .getDescriptionBox()
                        .getLabel()
                        .equals(label));
    }

    public void removeGatheredAssertion(String label) {
        this.claim.getGatheredAssertions()
                .removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.gatheredAssertions.entrySet().removeIf(
                entry -> entry.getKey()
                        .getDescriptionBox()
                        .getLabel()
                        .equals(label));

    }

    private void ensureLabelUniquenessInAssertionStore() throws MipamsException {

        List<JumbfBox> allAssertions = new ArrayList<>();
        allAssertions.addAll(this.gatheredAssertions.keySet());
        allAssertions.addAll(this.createdAssertions.keySet());

        Map<String, Long> assertionsLabelOccurenceMap = JpegTrustUtils
                .computeDuplicateLabelOccurrenceMap(allAssertions);

        boolean hasDuplicateAssertionLabel = assertionsLabelOccurenceMap.values().stream().anyMatch(v -> v > 1);

        if (hasDuplicateAssertionLabel) {
            throw new MipamsException("There are duplicate assertion labels in the Assertion Store");
        }
    }
}
