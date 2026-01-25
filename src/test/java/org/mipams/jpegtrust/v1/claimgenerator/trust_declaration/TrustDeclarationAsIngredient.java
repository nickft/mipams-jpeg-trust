package org.mipams.jpegtrust.v1.claimgenerator.trust_declaration;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.v1.claimgenerator.ManifestScenarios;
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
public class TrustDeclarationAsIngredient {

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
    void testTrustDeclarationAsIngredientJpeg1() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jpeg", "s12-trust-declaration-as-ingredient.jpeg");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jpeg");

        jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);

        String jsonFilePath = targetFileUrl.replace(".jpeg", "-jpeg.json");
        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testTrustDeclarationAsIngredientJxl() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jxl").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jxl", "s12-trust-declaration-as-ingredient.jxl");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jxl");

        jXlGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jxl", "-jxl.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testTrustDeclarationAsIngredientJp2() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jp2").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jp2", "s12-trust-declaration-as-ingredient.jp2");

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

        final IngredientAssertionV1 ingredientAssertion = ManifestScenarios.getIngredientAssertion(
                ingredientManifest, jumbfBoxDigestService, mediaType);

        final Assertion actions = ManifestScenarios.getActionsWithIngredientAssertion(ingredientManifest,
                jumbfBoxDigestService);

        ManifestScenarios manifestScenarios = new ManifestScenarios();
        manifestScenarios.setJumbfBoxDigestService(jumbfBoxDigestService);

        JumbfBox manifestBox = manifestScenarios.getManifestWithAssertions(assetFileUrl, mediaType,
                List.of(actions, ingredientAssertion), ingredientManifest);
        return JpegTrustUtils.buildTrustRecord(ingredientManifest, manifestBox);
    }

    private JumbfBox getIngredientManifest(String assetFileUrl, String mediaType) throws Exception {
        ManifestScenarios manifestScenarios = new ManifestScenarios();
        manifestScenarios.setJumbfBoxDigestService(jumbfBoxDigestService);

        JumbfBox trustDeclaration = manifestScenarios.getTrustDeclarationManifest(assetFileUrl, mediaType);

        return trustDeclaration;
    }
}