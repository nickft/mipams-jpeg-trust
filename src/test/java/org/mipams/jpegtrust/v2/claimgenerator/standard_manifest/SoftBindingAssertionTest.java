package org.mipams.jpegtrust.v2.claimgenerator.standard_manifest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion.SoftBindingBlock;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion.SoftBindingScope;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ParametersMap;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.enums.RangeChoice;
import org.mipams.jpegtrust.entities.assertions.enums.ShapeChoice;
import org.mipams.jpegtrust.entities.assertions.enums.UnitChoice;
import org.mipams.jpegtrust.entities.assertions.region.Range;
import org.mipams.jpegtrust.entities.assertions.region.Region;
import org.mipams.jpegtrust.entities.assertions.region.Shape;
import org.mipams.jpegtrust.entities.assertions.region.Shape.Coordinate;
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
public class SoftBindingAssertionTest {

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    JpegCodestreamParser parser;

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Test
    void testActionsAssertion() throws Exception {
        String initialAssetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();
        String assetFileUrl = initialAssetFileUrl.replace("sample", "s06");
        jpegCodestreamGenerator.stripJumbfMetadataWithUuidEqualTo(initialAssetFileUrl, assetFileUrl,
                new TrustRecordContentType().getContentTypeUuid());

        JumbfBox trustRecord = constructTrustRecordForScenario(assetFileUrl);

        String targetFileUrl = assetFileUrl.replace(".jpeg", "-standard-manifest-with-software-binding-2ed.jpeg");
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
        ActionAssertion assertion1 = new ActionAssertion();
        assertion1.setAction(ActionChoice.C2PA_CREATED.getValue());
        assertion1.setSoftwareAgent("Image Editing Tool");
        assertion1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        ParametersMap map = new ParametersMap();
        map.setParameters(Map.of("instanceID", "xmp:iid:e928fac1-8473-4c70-1982-369e91d4e58d"));
        assertion1.setParameters(map);

        ActionAssertion assertion2 = new ActionAssertion();
        assertion2.setAction(ActionChoice.C2PA_CONVERTED.getValue());
        assertion2.setSoftwareAgent("Image Editing Tool");
        assertion2.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        assertion2.setParameters(map);

        ActionsAssertion actions = new ActionsAssertion();
        actions.setActions(List.of(assertion1, assertion2));

        final SoftBindingAssertion assertion = getSoftBindingAssertion();

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        contentBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        final ManifestBuilder builder = new ManifestBuilder(new StandardManifestContentType());
        builder.addCreatedAssertion(actions);
        builder.addCreatedAssertion(assertion);
        builder.addCreatedAssertion(contentBindingAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setAlgorithm("sha256");

        List<X509Certificate> certificates = CryptoUtils.getCertificate();
        builder.setClaimSignatureCertificates(certificates);

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(builder.build());
        long totalBytesRequired = JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl);
        contentBindingAssertion.setDigest(digest);
        contentBindingAssertion.addExclusionRange((int) totalBytesRequired, 2);

        int paddingSize = Utils.calculateMinimumBytesRequired(2, (int) totalBytesRequired);
        pad = new byte[paddingSize];

        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        builder.removeCreatedAssertion(AssertionDiscovery.MipamsAssertion.CONTENT_BINDING.getBaseLabel());
        builder.addCreatedAssertion(contentBindingAssertion);

        PrivateKey privKey = CryptoUtils
                .getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        JumbfBox trustRecord = JpegTrustUtils.buildTrustRecord(builder.build());
        return trustRecord;
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
