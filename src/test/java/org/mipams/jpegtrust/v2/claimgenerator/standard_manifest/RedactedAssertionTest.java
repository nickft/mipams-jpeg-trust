package org.mipams.jpegtrust.v2.claimgenerator.standard_manifest;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.builders.ManifestBuilder;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.DigestResultForJumbfBox;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ParametersMap;
import org.mipams.jpegtrust.entities.assertions.cawg.MetadataAssertion;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.jpeg_systems.content_types.AssertionStoreContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.utils.CryptoUtils;
import org.mipams.jpegtrust.utils.Utils;
import org.mipams.jpegtrust.v2.claimgenerator.ManifestScenarios;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.Jp2CodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegXLGenerator;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.privsec.config.PrivsecConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JumbfConfig.class, PrivsecConfig.class, JpegTrustConfig.class })
@ActiveProfiles("test")
public class RedactedAssertionTest {

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    JpegXLGenerator jXlGenerator;

    @Autowired
    Jp2CodestreamGenerator jp2Generator;

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Autowired
    JumbfBoxDigestService jumbfBoxDigestService;

    @Test
    void testRedactingAssertionJpeg1() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jpeg", "s08-redacting-assertion-2ed.jpeg");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jpeg");

        jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);

        String jsonFilePath = targetFileUrl.replace(".jpeg", "-jpeg.json");
        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testRedactingAssertiontsJxl() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jxl").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jxl", "s09-redacting-assertion-2ed.jxl");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jxl");

        jXlGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jxl", "-jxl.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testRedactingAssertiontsJp2() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jp2").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jp2", "s09-redacting-assertion-2ed.jp2");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jp2");

        jp2Generator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jp2", "-jp2.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    private JumbfBox constructTrustRecordForScenario(String assetFileUrl, String mediaType) throws Exception {

        JumbfBox ingredientManifest = getIngredientManifest(assetFileUrl, mediaType);

        ActionAssertion assertion1 = new ActionAssertion();
        assertion1.setAction(ActionChoice.C2PA_OPENED.getValue());
        assertion1.setSoftwareAgent("Image Editing Tool");
        assertion1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        ParametersMap map = new ParametersMap();
        map.setParameters(Map.of("instanceID", "xmp:iid:e928fac1-8473-4c70-1982-369e91d4e58d"));

        final DigestResultForJumbfBox locallyComputedHash = jumbfBoxDigestService
                .calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference
                .setUrl(String.format("self#jumbf=c2pa.assertions/c2pa.ingredient.v3"));
        hashedUriReference.setDigest(locallyComputedHash.getDigest());

        map.setIngredients(List.of(hashedUriReference));
        assertion1.setParameters(map);

        ActionsAssertion actions = new ActionsAssertion();
        actions.setActions(List.of(assertion1));

        final BindingAssertion tempBindingAssertion = new BindingAssertion();
        tempBindingAssertion.setAlgorithm("sha256");
        tempBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        tempBindingAssertion.setPadding(pad);

        final IngredientAssertion ingredientAssertion = ManifestScenarios.getIngredientAssertion(ingredientManifest,
                jumbfBoxDigestService, mediaType);

        final ManifestBuilder builder = new ManifestBuilder(new StandardManifestContentType());
        builder.setTitle("MIPAMS test image with ingredient");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setAlgorithm("sha256");

        List<X509Certificate> certificates = CryptoUtils.getCertificate();
        builder.setClaimSignatureCertificates(certificates);

        for (Assertion assertion : List.of(actions, ingredientAssertion, tempBindingAssertion)) {
            JumbfBox assertionBox = assertion.toJumbfBox();
            DigestResultForJumbfBox digestResult = jumbfBoxDigestService
                    .calculateDigestForJumbfBox(assertionBox);

            builder.addCreatedAssertion(assertionBox, digestResult);
        }

        String redactedUri = String.format("self#jumbf=/c2pa/%s/%s/cawg.metadata",
                ingredientManifest.getDescriptionBox().getLabel(),
                (new AssertionStoreContentType()).getLabel());
        builder.addRedactedAssertion(redactedUri);

        JumbfBox assertionStoreJumbfBox = JpegTrustUtils
                .locateJpegTrustJumbfBoxByContentType(ingredientManifest,
                        (new AssertionStoreContentType()));

        assertionStoreJumbfBox.getContentBoxList()
                .removeIf(jumbfBox -> "cawg.metadata"
                        .equals(((JumbfBox) jumbfBox).getDescriptionBox().getLabel()));

        assertionStoreJumbfBox.updateFieldsBasedOnExistingData();

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(ingredientManifest, builder.build());
        long totalBytesRequired = (mediaType.endsWith("jxl") || assetFileUrl.endsWith("jp2"))
                ? tempTrustRecord.getBoxSizeFromBmffHeaders()
                : JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        BindingAssertion contentBindingAssertion = Utils.getBindingAssertionForAsset(assetFileUrl,
                totalBytesRequired);

        builder.removeCreatedAssertion(AssertionDiscovery.MipamsAssertion.CONTENT_BINDING.getBaseLabel());

        JumbfBox assertionBox = contentBindingAssertion.toJumbfBox();
        DigestResultForJumbfBox digestResult = jumbfBoxDigestService
                .calculateDigestForJumbfBox(assertionBox);

        builder.addCreatedAssertion(assertionBox, digestResult);

        PrivateKey privKey = CryptoUtils
                .getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(CryptoUtils.decodeFromDER(signature.sign(), 32));

        JumbfBox trustRecord = JpegTrustUtils.buildTrustRecord(ingredientManifest, builder.build());
        return trustRecord;
    }

    private JumbfBox getIngredientManifest(String assetFileUrl, String mediaType) throws Exception {
        ManifestScenarios manifestScenarios = new ManifestScenarios();
        manifestScenarios.setJumbfBoxDigestService(jumbfBoxDigestService);

        Assertion actions = ManifestScenarios.getInitialActions();

        MetadataAssertion assertion = new MetadataAssertion();
        assertion.setPayload(getCawgMetadataPayload());

        return manifestScenarios.getManifestWithAssertions(assetFileUrl, mediaType,
                List.of(actions, assertion));
    }

    private byte[] getCawgMetadataPayload() {
        String cawgMetadata = "{ \"@context\" : { \"Iptc4xmpCore\": \"http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/\", \"Iptc4xmpExt\": \"http://iptc.org/std/Iptc4xmpExt/2008-02-29/\", \"dc\" : \"http://purl.org/dc/elements/1.1/\", \"dcterms\" : \"http://purl.org/dc/terms/\"}, \"Iptc4xmpExt:LocationCreated\": { \"Iptc4xmpExt:City\": \"San Francisco\" }, \"Iptc4xmpCore:copyrightNotice\": \"Copyright © 2021 Example Corp. News\", \"dcterms:dateCopyrighted\": \"2021-05-20\", \"dcterms:license\": \"https://creativecommons.org/licenses/by/4.0/\", \"dc:subject\": [\"San Francisco\", \"Golden Gate Bridge\"]}";
        return cawgMetadata.getBytes();
    }
}
