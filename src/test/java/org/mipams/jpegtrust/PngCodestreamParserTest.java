package org.mipams.jpegtrust;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.jpeg_systems.PngCodestreamParser;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
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
@Disabled("Disabled: A test .png file must be provided under src/test/resources to run this parser test.")
public class PngCodestreamParserTest {

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Autowired
    JpegCodestreamParser parser;

    @Autowired
    JpegCodestreamGenerator generator;

    @Autowired
    JumbfBoxDigestService jumbfBoxDigestService;

    @Autowired
    PngCodestreamParser pngCodestreamParser;

    @Test
    void testCreationOfTrustIndicatorsWithManifestV1() throws Exception {
        String asset = ResourceUtils.getFile("classpath:test.png").getAbsolutePath();

        List<JumbfBox> results = pngCodestreamParser.parseMetadataFromFile(asset);
        TrustIndicatorSet set = manifestStoreConsumer.validate(results.getFirst(), asset);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String test = mapper.writeValueAsString(set);
        assertNotNull(test);
    }

}
