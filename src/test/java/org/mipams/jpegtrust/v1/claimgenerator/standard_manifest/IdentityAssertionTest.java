package org.mipams.jpegtrust.v1.claimgenerator.standard_manifest;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.cawg.IdentityAssertion;
import org.mipams.jpegtrust.entities.assertions.cawg.IdentityAssertion.SignerPayload;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.builders.ManifestBuilderV1;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertionV1;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertionV1;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.jpt.RightsAssertion;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.utils.CryptoUtils;
import org.mipams.jpegtrust.utils.Utils;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamParser;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.privsec.config.PrivsecConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JumbfConfig.class, PrivsecConfig.class, JpegTrustConfig.class })
@ActiveProfiles("test")
public class IdentityAssertionTest {

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    JpegCodestreamParser parser;

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Test
    void testActionsAssertion() throws Exception {
        String initialAssetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String assetFileUrl = initialAssetFileUrl.replace("sample", "s07");
        jpegCodestreamGenerator.stripJumbfMetadataWithUuidEqualTo(initialAssetFileUrl, assetFileUrl,
                new TrustRecordContentType().getContentTypeUuid());

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl);

        String targetFileUrl = assetFileUrl.replace(".jpeg",
                "-standard-manifest-with-rights-and-identity-assertion.jpeg");
        jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String test = mapper.writeValueAsString(set);

        String jsonFilePath = targetFileUrl.replace(".jpeg", ".json");
        Path outputPath = Paths.get(jsonFilePath);
        Files.write(outputPath, test.getBytes());

        assertNotNull(test);

        CoreUtils.deleteFile(assetFileUrl);
    }

    private JumbfBox constructTrustRecordForScenario(String assetFileUrl) throws Exception {
        ActionAssertionV1 assertion1 = new ActionAssertionV1();
        assertion1.setAction(ActionChoice.C2PA_CREATED.getValue());
        assertion1.setSoftwareAgent("Image Editing Tool");
        assertion1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        assertion1.setParameters(Map.of("instanceID", "ed610ae51f604002be3dbf0c589a2f1f"));

        ActionsAssertionV1 actions = new ActionsAssertionV1();
        actions.setActions(List.of(assertion1));

        RightsAssertion rightsAssertion = new RightsAssertion();
        rightsAssertion.setPayload(getOdrlRightsDeclaration());

        IdentityAssertion identityAssertion = getIdentityAssertion(List.of(actions, rightsAssertion));

        final BindingAssertion placeHolderContentBindingAssertion = new BindingAssertion();
        placeHolderContentBindingAssertion.setAlgorithm("sha256");
        placeHolderContentBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        placeHolderContentBindingAssertion.setPadding(pad);

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new StandardManifestContentType());
        builder.addAssertion(actions);
        builder.addAssertion(rightsAssertion);
        builder.addAssertion(placeHolderContentBindingAssertion);
        builder.addAssertion(identityAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setMediaType("image/jpeg");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setAlgorithm("sha256");

        List<X509Certificate> certificates = CryptoUtils.getCertificate();
        builder.setClaimSignatureCertificates(certificates);

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(builder.build());

        BindingAssertion finalizedContentBindingAssertion = calculateContentBindingAssertion(tempTrustRecord,
                assetFileUrl);

        builder.removeAssertion(AssertionDiscovery.MipamsAssertion.CONTENT_BINDING.getBaseLabel());
        builder.addAssertion(finalizedContentBindingAssertion);

        builder.removeAssertion(AssertionDiscovery.MipamsAssertion.CAWG_IDENTIY.getBaseLabel());

        PrivateKey privKey = CryptoUtils
                .getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        Signature identitySignature = Signature.getInstance("SHA256withECDSA");
        identitySignature.initSign(privKey);
        identitySignature.update(getVCUnprotectedPart());

        byte[] signaturePayload = CoseUtils.toIdentityAssertionCose(getVCUnprotectedPart(), identitySignature.sign());

        int signatureSize = signaturePayload.length;
        int sizeOfUpdatedPadding = identityAssertion.getPad1().length - (signatureSize
                - identityAssertion.getSignature().length + 1);

        identityAssertion.setSignature(signaturePayload);
        if (sizeOfUpdatedPadding < 0) {
            throw new MipamsException(
                    "Couldn't not adjust the size of the Identity Assertion in a multi-step " +
                            "process of the JPEG Trust Manifest. Consider increasing the padding size of the placeholder identity assertion.");
        }

        byte[] identityPad = new byte[sizeOfUpdatedPadding];
        Arrays.fill(identityPad, Byte.parseByte("0"));
        identityAssertion.setPad1(identityPad);

        builder.addAssertion(identityAssertion);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        JumbfBox trustRecord = JpegTrustUtils.buildTrustRecord(builder.build());
        return trustRecord;

    }

    private BindingAssertion calculateContentBindingAssertion(JumbfBox tempTrustRecord, String assetFileUrl)
            throws Exception {
        long totalBytesRequired = JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl);
        BindingAssertion contentBindingAssertion = new BindingAssertion();

        contentBindingAssertion.setAlgorithm("sha256");
        contentBindingAssertion.setDigest(digest);
        contentBindingAssertion.addExclusionRange((int) totalBytesRequired, 2);

        int paddingSize = Utils.calculateMinimumBytesRequired(2, (int) totalBytesRequired);
        byte[] pad = new byte[paddingSize];

        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        return contentBindingAssertion;
    }

    private IdentityAssertion getIdentityAssertion(List<Assertion> referencedAssertions) throws MipamsException {

        LinkedHashSet<HashedUriReference> result = new LinkedHashSet<>();
        for (Assertion assertion : referencedAssertions) {

            final byte[] locallyComputedHash = CoseUtils.getByteArray(32);

            final HashedUriReference hashedUriReference = new HashedUriReference();
            hashedUriReference.setUrl(
                    String.format("self#jumbf=c2pa.assertions/%s", assertion.getLabel()));
            hashedUriReference.setDigest(locallyComputedHash);
            hashedUriReference.setAlgorithm("sha256");

            result.add(hashedUriReference);
        }

        SignerPayload signerPayload = new SignerPayload();
        signerPayload.setSigningType("cawg.identity_claims_aggregation");
        signerPayload.setReferencedAssertions(result);

        IdentityAssertion assertion = new IdentityAssertion();
        assertion.setSignerPayload(signerPayload);
        assertion.setSignature(new byte[10]);
        byte[] pad = new byte[1024];
        Arrays.fill(pad, Byte.parseByte("0"));
        assertion.setPad1(pad);

        return assertion;
    }

    private byte[] getOdrlRightsDeclaration() {
        String rightsOdrl = "{ \"@context\": \"http://www.w3.org/ns/odrl.jsonld\", \"@type\": \"Agreement\", \"uid\": \"http://example.com/policy:42\", \"profile\": \"http://example.com/odrl:profile:09\",\"obligation\": [{ \"assigner\": \"http://example.com/org:43\", \"assignee\": \"http://example.com/person:44\", \"action\": [{ \"rdf: value\": { \"@id\": \"odrl: compensate\" }, \"refinement\": [{ \"leftOperand\": \"payAmount\", \"operator\": \"eq\", \"rightOperand\": { \"@value\": \"5.00\", \"@type\": \"xsd:decimal\" }, \"unit\": \"http://dbpedia.org/resource/Euro\" }] }] }]}";
        return rightsOdrl.getBytes();
    }

    private byte[] getVCUnprotectedPart() {
        String vcPayload = "{\\\"@context\\\":[\\\"https://www.w3.org/ns/credentials/v2\\\",\\\"https://www.w3.org/ns/credentials/examples/v2\\\"],\\\"id\\\":\\\"http://university.example/credentials/3732\\\",\\\"type\\\":[\\\"VerifiableCredential\\\",\\\"ExampleDegreeCredential\\\",\\\"ExamplePersonCredential\\\"],\\\"issuer\\\":\\\"https://university.example/issuers/14\\\",\\\"validFrom\\\":\\\"2010-01-01T19:23:24Z\\\",\\\"credentialSubject\\\":{\\\"id\\\":\\\"did:example:ebfeb1f712ebc6f1c276e12ec21\\\",\\\"degree\\\":{\\\"type\\\":\\\"ExampleBachelorDegree\\\",\\\"name\\\":\\\"Bachelor of Science and Arts\\\"},\\\"alumniOf\\\":{\\\"name\\\":\\\"Example University\\\"}},\\\"credentialSchema\\\":[{\\\"id\\\":\\\"https://example.org/examples/degree.json\\\",\\\"type\\\":\\\"JsonSchema\\\"},{\\\"id\\\":\\\"https://example.org/examples/alumni.json\\\",\\\"type\\\":\\\"JsonSchema\\\"}]}";
        return vcPayload.getBytes();
    }

}
