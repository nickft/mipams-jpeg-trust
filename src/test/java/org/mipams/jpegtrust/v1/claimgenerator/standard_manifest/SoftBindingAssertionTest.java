package org.mipams.jpegtrust.v1.claimgenerator.standard_manifest;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion.SoftBindingBlock;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion.SoftBindingScope;
import org.mipams.jpegtrust.entities.assertions.enums.RangeChoice;
import org.mipams.jpegtrust.entities.assertions.enums.ShapeChoice;
import org.mipams.jpegtrust.entities.assertions.enums.UnitChoice;
import org.mipams.jpegtrust.entities.assertions.region.Range;
import org.mipams.jpegtrust.entities.assertions.region.Region;
import org.mipams.jpegtrust.entities.assertions.region.Shape;
import org.mipams.jpegtrust.entities.assertions.region.Shape.Coordinate;
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
public class SoftBindingAssertionTest {

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
    void testSoftBindingAssertionJpeg1() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jpeg", "s06-soft-binding.jpeg");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jpeg");

        jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);

        String jsonFilePath = targetFileUrl.replace(".jpeg", "-jpeg.json");
        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testSoftBindingAssertionJxl() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jxl").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jxl", "s06-soft-binding.jxl");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jxl");

        jXlGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jxl", "-jxl.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testSoftBindingAssertionJp2() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jp2").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jp2", "s06-soft-binding.jp2");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jp2");

        jp2Generator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jp2", "-jp2.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    private JumbfBox constructTrustRecordForScenario(String assetFileUrl, String mediaType) throws Exception {
        ManifestScenarios manifestScenarios = new ManifestScenarios();
        manifestScenarios.setJumbfBoxDigestService(jumbfBoxDigestService);

        Assertion actions = ManifestScenarios.getInitialActions();

        final SoftBindingAssertion assertion = getSoftBindingAssertion();

        JumbfBox manifestBox = manifestScenarios.getManifestWithAssertions(assetFileUrl, mediaType,
                List.of(actions, assertion));
        return JpegTrustUtils.buildTrustRecord(manifestBox);
    }

    private SoftBindingAssertion getSoftBindingAssertion() {

        Coordinate coord = new Coordinate();
        coord.setX(0f);
        coord.setY(0f);

        Shape shape = new Shape();
        shape.setType(ShapeChoice.RECTANGLE);
        shape.setUnit(UnitChoice.PERCENT);
        shape.setOrigin(coord);
        shape.setWidth(100f);
        shape.setHeight(100f);

        Range range = new Range();
        range.setRange(RangeChoice.SPATIAL);
        range.setShape(shape);

        Region region = new Region();
        region.getRegion().add(range);

        SoftBindingScope scope = new SoftBindingScope();
        scope.setRegion(region);

        SoftBindingBlock block = new SoftBindingBlock();
        block.setValue("ISCC:KEDT7P27F67376KXKVGT4EKEIPZZH6MOWRE2UNKWJHOKDTCPZN4RWGF4GZG4XUEPS3DA");
        block.setScope(scope);

        SoftBindingAssertion softBindingAssertion = new SoftBindingAssertion();
        softBindingAssertion.setAlgorithm("ISCC");
        softBindingAssertion.setPadding(new byte[0]);
        softBindingAssertion.getBlocks().add(block);

        return softBindingAssertion;
    }
}
