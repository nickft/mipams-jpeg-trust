package org.mipams.jpegtrust.builders;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.IngredientAssertion;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustManifestContentType;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.DescriptionBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ManifestBuilder {

    private String uuid;
    private JumbfBoxBuilder assertionStoreJumbfBoxBuilder;
    private Claim claim;
    private byte[] claimSignature;
    private List<X509Certificate> claimSignatureCertificates;
    private Map<String, JumbfBox> assertionMap;
    private TrustManifestContentType trustManifestContentType;

    public ManifestBuilder(TrustManifestContentType trustManifestContentType) {
        this.trustManifestContentType = trustManifestContentType;
        this.uuid = issueNewManifestId();
        this.claim = new Claim();
        this.claim.setSignature("self#jumbf=c2pa.signature");
        this.claimSignature = new byte[0];
        this.assertionMap = new HashMap<>();
    }

    public ManifestBuilder addAssertion(JumbfBox assertionJumbfBox, HashedUriReference hashedUriReference) throws Exception {
        hashedUriReference.setUrl(String.format("self#jumbf=/c2pa/%s/c2pa.assertions/%s", this.uuid, assertionJumbfBox.getDescriptionBox().getLabel()));

        byte[] locallyComputedHash = calculateDigestForJumbfBox(assertionJumbfBox);

        System.out.println(String.format("Locally computed: %s", CoreUtils.convertByteArrayToHex(locallyComputedHash)));

        System.out.println(String.format("Externally computed: %s", CoreUtils.convertByteArrayToHex(hashedUriReference.getDigest())));

        claim.getAssertions().add(hashedUriReference);
        return this;
    }

    public ManifestBuilder addIngredientAssertion(IngredientAssertion ingredientAssertion, JumbfBox ingredientManifest) throws MipamsException {
        if(ingredientManifest.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Ingredient manifest with no label %s", ingredientManifest.toString()));
        }
        final byte[] locallyComputedHash = calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference.setUrl(String.format("self#jumbf=/c2pa/%s", ingredientManifest.getDescriptionBox().getLabel()));
        hashedUriReference.setDigest(locallyComputedHash);

        ingredientAssertion.setManifestReference(hashedUriReference);

        addAssertion(ingredientAssertion);

        return this;
    }

    public ManifestBuilder addAssertion(Assertion assertion) throws MipamsException {
        JumbfBox assertionJumbfBox = assertion.toJumbfBox();

        if(assertionJumbfBox.getDescriptionBox().getLabel() == null) {
            throw new MipamsException(String.format("Assertion with no label %s", assertionJumbfBox.toString()));
        }

        final byte[] locallyComputedHash = calculateDigestForJumbfBox(assertionJumbfBox);

        assertionMap.put(CoreUtils.convertByteArrayToHex(locallyComputedHash), assertionJumbfBox);

        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference.setUrl(String.format("self#jumbf=/c2pa/%s/c2pa.assertions/%s", this.uuid, assertionJumbfBox.getDescriptionBox().getLabel()));
        hashedUriReference.setDigest(locallyComputedHash);

        this.claim.getAssertions().add(hashedUriReference);
        return this;
    }

    public ManifestBuilder addRedactedAssertion(String jumbfUriReference) throws MipamsException {
        if (claim.getRedactedAssertions() == null) {
            claim.setRedactedAssertions(new ArrayList<>());
        }

        claim.getRedactedAssertions().add(jumbfUriReference);
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
        this.claim.setClaimGenerator(generatorInfoName);
        return this;
    }

    public ManifestBuilder setMediaType(String mediaType) throws MipamsException {
        this.claim.setMediaType(mediaType);
        return this;
    }

    public byte[] encodeClaimToBeSigned() throws MipamsException{
        try {
            ensureLabelUniquenessInAssertionStore();
            return CoseUtils.toSigStructure(claim, claimSignatureCertificates);
        } catch (Exception e){
            throw new MipamsException(e);
        }
    }

    public ManifestBuilder setClaimSignature(byte[] coseEncodedByteArray) throws MipamsException{
        this.claimSignature = coseEncodedByteArray;
        return this;
    }

    public ManifestBuilder setClaimSignatureCertificates(List<X509Certificate> certificates) throws MipamsException{
        this.claimSignatureCertificates = new ArrayList<>(certificates);
        return this;
    }

    public JumbfBox build() throws MipamsException{
        if(assertionMap.isEmpty()){
            throw new MipamsException("Empty manifest: No assertions specified");
        }

        if(assertionMap.isEmpty()){
            throw new MipamsException("Empty manifest: No assertions specified");
        }
        // NULL checks

        final AssertionStoreContentType assertionStoreContentType = new AssertionStoreContentType();
        assertionStoreJumbfBoxBuilder = new JumbfBoxBuilder(assertionStoreContentType);
        assertionStoreJumbfBoxBuilder.setJumbfBoxAsRequestable();
        assertionStoreJumbfBoxBuilder.setLabel(assertionStoreContentType.getLabel());
        assertionStoreJumbfBoxBuilder.appendAllContentBoxes(new ArrayList<>(assertionMap.values()));

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
        return String.format("urn:uuid:%s", CoreUtils.randomStringGenerator());
    }

    private JumbfBox getClaimSignatureJumbfBox() throws MipamsException {
        final ClaimSignatureContentType claimSignatureContentType = new ClaimSignatureContentType();
        
        final JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(claimSignatureContentType);
        jumbfBoxBuilder.setJumbfBoxAsRequestable();
        jumbfBoxBuilder.setLabel(claimSignatureContentType.getLabel());

        final byte[] coseSign1 = CoseUtils.toCoseSign1EncodedBytestream(this.claim, this.claimSignatureCertificates, this.claimSignature);

        CborBox cborBox = new CborBox();
        cborBox.setContent(coseSign1);
        jumbfBoxBuilder.appendContentBox(cborBox);

        return jumbfBoxBuilder.getResult();
    }

    public void removeAssertion(String label) {
        this.claim.getAssertions().removeIf(hashedUri -> hashedUri.getUrl().contains(label));

        this.assertionMap = this.assertionMap.entrySet().stream()
                .filter(entrySet -> !entrySet.getValue().getDescriptionBox().getLabel().equals(label))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
    }

    private byte[] calculateDigestForJumbfBox(JumbfBox jumbfBox) throws MipamsException {
        String tempFilePath = "";
        try {

            String tempFile = CoreUtils.randomStringGenerator();
            tempFilePath = CoreUtils.createTempFile(tempFile, CoreUtils.JUMBF_FILENAME_SUFFIX);

            ApplicationContext context = new AnnotationConfigApplicationContext(JpegTrustConfig.class, JumbfConfig.class);
            CoreGeneratorService coreGeneratorService = context.getBean(CoreGeneratorService.class);
            coreGeneratorService.generateJumbfMetadataToFile(List.of(jumbfBox), tempFilePath);
            
            ((ConfigurableApplicationContext)context).close();

            try (FileInputStream fis = new FileInputStream(tempFilePath)) {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
    
                byte[] buffer = new byte[128];

                fis.skip((jumbfBox.isXBoxEnabled() ? 16 : 8));
    
                while (fis.available() > 0) {
                    int l = fis.read(buffer);
                    sha.update(buffer, 0, l);
                }
    
                return sha.digest();
            }
        } catch (Exception e) {
            throw new MipamsException(e);
        } finally {
            CoreUtils.deleteFile(tempFilePath);
        }
    }

    private void ensureLabelUniquenessInAssertionStore() throws MipamsException {
        Map<String, Long> labelOccurenceMap = computeDuplicateLabelOccurrenceMap();

        for (Entry<String, JumbfBox> assertionEntry : assertionMap.entrySet()) {
            final JumbfBox assertion = assertionEntry.getValue();
            final String commonLabel = assertion.getDescriptionBox().getLabel();
            Long occurences = labelOccurenceMap.get(commonLabel);

            if (occurences == null) {
                continue;
            }

            String uniqueLabel = String.format("%s__%d", commonLabel, occurences);
            assertion.getDescriptionBox().setLabel(uniqueLabel);
            assertion.getDescriptionBox().applyInternalBoxFieldsBasedOnExistingData();

            HashedUriReference hashedUri = this.claim.getAssertions().stream().filter(hashedUriRef -> CoreUtils.convertByteArrayToHex(hashedUriRef.getDigest()).equals(assertionEntry.getKey())).findAny().orElseThrow(() -> new MipamsException("Assertion store contains assertions that are not reported in the claim"));
            hashedUri.setUrl(String.format("self#jumbf=/c2pa/%s/c2pa.assertions/%s", this.uuid, assertion.getDescriptionBox().getLabel()));

            labelOccurenceMap.put(commonLabel, --occurences);
        }
    }

    private Map<String, Long> computeDuplicateLabelOccurrenceMap()
            throws MipamsException {
        return assertionMap.values().stream().map(box -> box.getDescriptionBox())
            .collect(Collectors.groupingBy(DescriptionBox::getLabel, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() >= 2)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
