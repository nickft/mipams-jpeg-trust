package org.mipams.jpegtrust.builders;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.ClaimV1;
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

public class ManifestBuilderV1 {

    private String uuid;
    private JumbfBoxBuilder assertionStoreJumbfBoxBuilder;
    private ClaimV1 claim;
    private byte[] claimSignature;
    private List<X509Certificate> claimSignatureCertificates;
    private LinkedHashMap<JumbfBox, HashedUriReference> assertionMap;
    private TrustManifestContentType trustManifestContentType;

    public ManifestBuilderV1(TrustManifestContentType trustManifestContentType) {
        this.trustManifestContentType = trustManifestContentType;
        this.uuid = issueNewManifestId();
        this.claim = new ClaimV1();
        this.claim.setSignature("self#jumbf=c2pa.signature");
        this.claimSignature = new byte[0];
        this.assertionMap = new LinkedHashMap<>();
    }

    public ManifestBuilderV1 addAssertion(JumbfBox assertionJumbfBox, DigestResultForJumbfBox digest)
            throws MipamsException {

        if (assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference.setAlgorithm(digest.getAlgorithm());
        hashedUriReference.setDigest(digest.getDigest());
        hashedUriReference.setUrl(
                String.format("self#jumbf=c2pa.assertions/%s", assertionJumbfBox.getDescriptionBox().getLabel()));

        this.assertionMap.put(assertionJumbfBox, hashedUriReference);
        return this;
    }

    public ManifestBuilderV1 addRedactedAssertion(String jumbfUriReference) throws MipamsException {
        if (claim.getRedactedAssertions() == null) {
            claim.setRedactedAssertions(new LinkedHashSet<>());
        }

        claim.getRedactedAssertions().add(jumbfUriReference);

        return this;
    }

    public ManifestBuilderV1 setInstanceID(String instanceID) throws MipamsException {
        this.claim.setInstanceId(instanceID);
        return this;
    }

    public ManifestBuilderV1 setTitle(String title) throws MipamsException {
        this.claim.setTitle(title);
        return this;
    }

    public ManifestBuilderV1 setGeneratorInfoName(String generatorInfoName) throws MipamsException {
        this.claim.setClaimGenerator(generatorInfoName);
        return this;
    }

    public ManifestBuilderV1 setMediaType(String mediaType) throws MipamsException {
        this.claim.setMediaType(mediaType);
        return this;
    }

    public ManifestBuilderV1 setAlgorithm(String hashingAlgorithm) throws MipamsException {
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
        this.claim.getAssertions().clear();
        this.assertionMap.values().forEach(val -> this.claim.getAssertions().add(val));
    }

    public ManifestBuilderV1 setClaimSignature(byte[] coseEncodedByteArray) throws MipamsException {
        this.claimSignature = coseEncodedByteArray;
        return this;
    }

    public ManifestBuilderV1 setClaimSignatureCertificates(List<X509Certificate> certificates) throws MipamsException {
        this.claimSignatureCertificates = new ArrayList<>(certificates);
        return this;
    }

    public JumbfBox build() throws MipamsException {
        ensureLabelUniquenessInAssertionStore();
        buildClaimAssertions();

        if (assertionMap.isEmpty()) {
            throw new MipamsException("Empty manifest: No assertions specified");
        }

        final AssertionStoreContentType assertionStoreContentType = new AssertionStoreContentType();
        assertionStoreJumbfBoxBuilder = new JumbfBoxBuilder(assertionStoreContentType);
        assertionStoreJumbfBoxBuilder.setJumbfBoxAsRequestable();
        assertionStoreJumbfBoxBuilder.setLabel(assertionStoreContentType.getLabel());
        assertionStoreJumbfBoxBuilder.appendAllContentBoxes(new ArrayList<>(assertionMap.keySet()));

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
        jumbfBoxBuilder.setLabel(claimContentType.getLabelV1());

        CborBox cborBox = new CborBox();
        cborBox.setContent(CoseUtils.toCborEncodedByteArray(this.claim));
        jumbfBoxBuilder.appendContentBox(cborBox);

        return jumbfBoxBuilder.getResult();
    }

    private static String issueNewManifestId() {
        return String.format("urn:uuid:%s", CoreUtils.randomStringGenerator());
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

    public void removeAssertion(String label) {
        this.claim.getAssertions()
                .removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.assertionMap.entrySet().removeIf(
                entry -> entry.getKey()
                        .getDescriptionBox()
                        .getLabel()
                        .equals(label));
    }

    private void ensureLabelUniquenessInAssertionStore() throws MipamsException {

        List<JumbfBox> allAssertions = new ArrayList<>(assertionMap.keySet());

        Map<String, Long> assertionsLabelOccurenceMap = JpegTrustUtils
                .computeDuplicateLabelOccurrenceMap(allAssertions);

        boolean hasDuplicateAssertionLabel = assertionsLabelOccurenceMap.values().stream().anyMatch(v -> v > 1);

        if (hasDuplicateAssertionLabel) {
            throw new MipamsException("There are duplicate assertion labels in the Assertion Store");
        }
    }
}
