package org.mipams.jpegtrust.v1.claimgenerator;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.builders.ManifestBuilderV1;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.ThumbnailAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertionV1;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertionV1;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustDeclarationContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.privsec.config.PrivsecConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JumbfConfig.class, PrivsecConfig.class, JpegTrustConfig.class})
@ActiveProfiles("test")
public class JumbfClaimGeneratorTest {

    @Autowired
    CoreGeneratorService coreGeneratorService;

    @Autowired
    CoreParserService coreParserService;

    @Test
    void testGenerationOfTrustRecord() throws Exception {
        List<X509Certificate> certificates = getCertificate();

        PrivateKey privKey =
                getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        ActionsAssertionV1 actions = new ActionsAssertionV1();
        ActionAssertionV1 assertion1 = new ActionAssertionV1();
        assertion1.setAction(ActionChoice.C2PA_FILTERED.getValue());
        assertion1.setSoftwareAgent("Adobe Photoshop");
        assertion1.setWhen("22/1/22 10:12:32");
        assertion1.setParameters(Map.of("instanceID", "ed610ae51f604002be3dbf0c589a2f1f"));

        ActionAssertionV1 assertion2 = new ActionAssertionV1();
        assertion2.setAction(ActionChoice.C2PA_CONVERTED.getValue());
        assertion2.setSoftwareAgent("Adobe Photoshop");
        assertion2.setWhen("22/1/22 10:42:32");
        assertion2.setParameters(Map.of("instanceID", "ed610ae51f604002be3dbf0c589a2f1f"));
        actions.setActions(List.of(assertion1, assertion2));

        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl, null);
        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        contentBindingAssertion.setDigest(digest);

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new StandardManifestContentType());
        builder.addAssertion(actions);
        builder.addAssertion(contentBindingAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setMediaType("image/jpeg");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setClaimSignatureCertificates(certificates);


        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        JumbfBoxBuilder trustRecordBuilder = new JumbfBoxBuilder(new TrustRecordContentType());
        trustRecordBuilder.setJumbfBoxAsRequestable();
        trustRecordBuilder.setLabel(new TrustRecordContentType().getLabel());

        JumbfBox manifest = builder.build();
        trustRecordBuilder.appendContentBox(manifest);

        JumbfBox trustRecord = trustRecordBuilder.getResult();

        final String outputJumbfFile =
                CoreUtils.randomStringGenerator() + "standard-manifest.jumbf";
        coreGeneratorService.generateJumbfMetadataToFile(List.of(trustRecord), outputJumbfFile);
    }

    @Test
    void testGenerationOfTrustDeclaration() throws Exception {
        List<X509Certificate> certificates = getCertificate();

        PrivateKey privKey =
                getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        ActionsAssertionV1 actions = new ActionsAssertionV1();
        ActionAssertionV1 assertion1 = new ActionAssertionV1();
        assertion1.setAction(ActionChoice.C2PA_CREATED.getValue());
        assertion1.setWhen("25/5/24 10:12:32");
        assertion1.setDigitalSourceType(
                "http://cv.iptc.org/newscodes/digitalsourcetype/algorithmicallyEnhanced");
        actions.setActions(List.of(assertion1));

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl, null);
        contentBindingAssertion.setDigest(digest);

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new TrustDeclarationContentType());
        builder.addAssertion(actions);
        builder.addAssertion(contentBindingAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setMediaType("image/jpeg");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setClaimSignatureCertificates(certificates);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        JumbfBoxBuilder trustRecordBuilder = new JumbfBoxBuilder(new TrustRecordContentType());
        trustRecordBuilder.setJumbfBoxAsRequestable();
        trustRecordBuilder.setLabel(new TrustRecordContentType().getLabel());

        JumbfBox manifest = builder.build();
        trustRecordBuilder.appendContentBox(manifest);

        JumbfBox trustRecord = trustRecordBuilder.getResult();

        final String outputJumbfFile =
                CoreUtils.randomStringGenerator() + "trust-decleration.jumbf";
        coreGeneratorService.generateJumbfMetadataToFile(List.of(trustRecord), outputJumbfFile);
    }

    @Test
    void testGenerationOfTrustRecordWithIngredientAssertion() throws Exception {
        List<X509Certificate> certificates = getCertificate();

        PrivateKey privKey =
                getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        JumbfBox ingredientManifest = getIngredientManifest(assetFileUrl);

        ActionsAssertionV1 actions = new ActionsAssertionV1();
        ActionAssertionV1 assertion2 = new ActionAssertionV1();
        assertion2.setAction(ActionChoice.C2PA_CONVERTED.getValue());
        assertion2.setSoftwareAgent("Adobe Photoshop");
        assertion2.setWhen("22/2/22 15:42:32");
        assertion2.setParameters(Map.of("instanceID", "ed610ae51f604002be3dbf0c589a2f1f"));
        actions.setActions(List.of(assertion2));

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl, null);
        contentBindingAssertion.setDigest(digest);

        final IngredientAssertionV1 ingredientAssertion = new IngredientAssertionV1();
        ingredientAssertion.setRelationship(IngredientAssertionV1.RELATIONSHIP_PARENT_OF);
        ingredientAssertion.setInstanceId("ab610ae5124002be3dbf0c589a2f1f");
        ingredientAssertion.setTitle("Ingredient manifest");
        ingredientAssertion.setMediaType("image/jpeg");

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new StandardManifestContentType());
        builder.setTitle("MIPAMS test image with ingredient");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setMediaType("image/jpeg");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setClaimSignatureCertificates(certificates);

        builder.addAssertion(actions);
        builder.addAssertion(contentBindingAssertion);
        builder.addIngredientAssertion(ingredientAssertion, ingredientManifest);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        JumbfBoxBuilder trustRecordBuilder = new JumbfBoxBuilder(new TrustRecordContentType());
        trustRecordBuilder.setJumbfBoxAsRequestable();
        trustRecordBuilder.setLabel(new TrustRecordContentType().getLabel());
        trustRecordBuilder.appendContentBox(ingredientManifest);
        trustRecordBuilder.appendContentBox(builder.build());

        JumbfBox trustRecord = trustRecordBuilder.getResult();

        final String outputJumbfFile =
                CoreUtils.randomStringGenerator() + "standard-manifest-with-ingredient.jumbf";
        coreGeneratorService.generateJumbfMetadataToFile(List.of(trustRecord), outputJumbfFile);
    }

    private JumbfBox getIngredientManifest(String assetFileUrl) throws Exception {
        ActionsAssertionV1 actions = new ActionsAssertionV1();

        ActionAssertionV1 assertion1 = new ActionAssertionV1();
        assertion1.setAction(ActionChoice.C2PA_FILTERED.getValue());
        assertion1.setSoftwareAgent("Adobe Photoshop");
        assertion1.setWhen("22/1/22 10:12:32");
        assertion1.setParameters(Map.of("instanceID", "ed610ae51f604002be3dbf0c589a2f1f"));

        actions.setActions(List.of(assertion1));

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl, null);
        contentBindingAssertion.setDigest(digest);

        final ThumbnailAssertion thumbnailAssertion = new ThumbnailAssertion();
        thumbnailAssertion.setThumbnailUrl(assetFileUrl);

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new StandardManifestContentType());
        builder.addAssertion(actions);
        builder.addAssertion(thumbnailAssertion);
        builder.addAssertion(contentBindingAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setMediaType("image/jpeg");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setClaimSignatureCertificates(getCertificate());

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(
                getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath()));
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        return builder.build();
    }

    private List<X509Certificate> getCertificate() throws Exception {
        try (FileInputStream fis = new FileInputStream(
                ResourceUtils.getFile("classpath:certs.pem").getAbsolutePath())) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificates(fis).stream().map(cert -> (X509Certificate) cert)
                    .collect(Collectors.toList());
        }
    }

    public PrivateKey getPrivateKey(String fileUrl) throws Exception {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(
                    "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgfNJBsaRLSeHizv0mGL+gcn78QmtfLSm+n+qG9veC2W2hRANCAAQPaL6RkAkYkKU4+IryBSYxJM3h77sFiMrbvbI8fG7w2Bbl9otNG/cch3DAw5rGAPV7NWkyl3QGuV/wt0MrAPDo");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
