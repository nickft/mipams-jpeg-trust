package org.mipams.jpegtrust;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.v2.claimgenerator.ManifestScenarios;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamParser;
import org.mipams.privsec.config.PrivsecConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JumbfConfig.class, PrivsecConfig.class, JpegTrustConfig.class })
@ActiveProfiles("test")
public class TrustIndicatorsTest {

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Autowired
    JpegCodestreamParser parser;

    @Autowired
    JpegCodestreamGenerator generator;

    @Autowired
    JumbfBoxDigestService jumbfBoxDigestService;

    @Test
    void testCreationOfTrustIndicatorsWithManifestV1() throws Exception {
        String asset = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        JumbfBox manifestStoreBox = constructTrustRecordForScenario(asset, "image/jpeg");

        String targetAsset = asset.replace(".jpeg", "-c2pa.jpeg");

        generator.generateJumbfMetadataToFile(List.of(manifestStoreBox), asset, targetAsset);

        List<JumbfBox> parsedBoxes = parser.parseMetadataFromFile(targetAsset);

        TrustIndicatorSet set = manifestStoreConsumer.validate(parsedBoxes.getFirst(), targetAsset);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String test = mapper.writeValueAsString(set);
        assertNotNull(test);
    }

    private JumbfBox constructTrustRecordForScenario(String assetFileUrl, String mediaType) throws Exception {

        ManifestScenarios manifestScenarios = new ManifestScenarios();
        manifestScenarios.setJumbfBoxDigestService(jumbfBoxDigestService);

        Assertion actionsAssertion = ManifestScenarios.getInitialActions();

        JumbfBox manifestBox = manifestScenarios.getManifestWithAssertions(assetFileUrl, mediaType,
                List.of(actionsAssertion));
        return JpegTrustUtils.buildTrustRecord(manifestBox);
    }
}
