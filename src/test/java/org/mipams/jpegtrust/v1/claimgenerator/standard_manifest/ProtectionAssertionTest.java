package org.mipams.jpegtrust.v1.claimgenerator.standard_manifest;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.cawg.MetadataAssertion;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
import org.mipams.jpegtrust.v1.claimgenerator.ManifestScenarios;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.Jp2CodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegXLGenerator;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.privsec.config.PrivsecConfig;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JumbfConfig.class, PrivsecConfig.class, JpegTrustConfig.class })
@ActiveProfiles("test")
public class ProtectionAssertionTest {

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
    void testProtectingAssertionJpeg1() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jpeg", "s09-protecting-assertion.jpeg");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jpeg");

        jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);

        String jsonFilePath = targetFileUrl.replace(".jpeg", "-jpeg.json");
        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testProtectingAssertionJxl() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jxl").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jxl", "s09-protecting-assertion.jxl");

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl, "image/jxl");

        jXlGenerator.generateJumbfMetadataToFile(List.of(trustRecord), assetFileUrl,
                targetFileUrl);

        TrustIndicatorSet set = manifestStoreConsumer.validate(trustRecord, targetFileUrl);
        String jsonFilePath = targetFileUrl.replace(".jxl", "-jxl.json");

        CoreUtils.writeBytesFromInputStreamToFile(new ByteArrayInputStream(set.toString().getBytes()), 0,
                jsonFilePath);
    }

    @Test
    void testProtectingAssertionJp2() throws Exception {
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jp2").getAbsolutePath();
        String targetFileUrl = assetFileUrl.replace("sample.jp2", "s09-protecting-assertion.jp2");

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

        JumbfBox protectedAssertion = getProtectedMetadata();

        JumbfBox manifestBox = manifestScenarios.getManifestWithAssertionBoxes(assetFileUrl, mediaType,
                List.of(actions.toJumbfBox(), protectedAssertion));
        return JpegTrustUtils.buildTrustRecord(manifestBox);
    }

    private JumbfBox getProtectedMetadata() throws MipamsException, FileNotFoundException {
        MetadataAssertion assertion = new MetadataAssertion();
        assertion.setPayload(getCawgMetadataPayload());

        JumbfBox result = assertion.toJumbfBox();

        JumbfBoxBuilder builder = new JumbfBoxBuilder(new ProtectionContentType());
        builder.setJumbfBoxAsRequestable();
        builder.setLabel(result.getDescriptionBox().getLabel());

        ProtectionDescriptionBox descriptionBox = new ProtectionDescriptionBox();
        descriptionBox.setAes256CbcProtection();

        BinaryDataBox binaryDataBox = new BinaryDataBox();
        binaryDataBox.setFileUrl(ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath());

        builder.appendContentBox(descriptionBox);
        builder.appendContentBox(binaryDataBox);

        return builder.getResult();
    }

    private byte[] getCawgMetadataPayload() {
        String cawgMetadata = "{ \"@context\" : { \"Iptc4xmpCore\": \"http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/\", \"Iptc4xmpExt\": \"http://iptc.org/std/Iptc4xmpExt/2008-02-29/\", \"dc\" : \"http://purl.org/dc/elements/1.1/\", \"dcterms\" : \"http://purl.org/dc/terms/\"}, \"Iptc4xmpExt:LocationCreated\": { \"Iptc4xmpExt:City\": \"San Francisco\" }, \"Iptc4xmpCore:copyrightNotice\": \"Copyright © 2021 Example Corp. News\", \"dcterms:dateCopyrighted\": \"2021-05-20\", \"dcterms:license\": \"https://creativecommons.org/licenses/by/4.0/\", \"dc:subject\": [\"San Francisco\", \"Golden Gate Bridge\"]}";
        return cawgMetadata.getBytes();
    }
}
