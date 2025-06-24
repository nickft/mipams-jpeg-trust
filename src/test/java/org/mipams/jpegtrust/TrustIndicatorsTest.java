package org.mipams.jpegtrust;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mipams.jpegtrust.builders.ManifestBuilderV1;
import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.ThumbnailAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.services.validation.consumer.ManifestStoreConsumer;
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
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

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

    @Test
    void testCreationOfTrustIndicatorsWithManifestV1() throws Exception {
        String asset = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        JumbfBox manifestStoreBox = getManifestStore(asset);

        String targetAsset = asset.replace(".jpeg", "-c2pa.jpeg");

        generator.generateJumbfMetadataToFile(List.of(manifestStoreBox), asset, targetAsset);

        List<JumbfBox> parsedBoxes = parser.parseMetadataFromFile(targetAsset);

        TrustIndicatorSet set = manifestStoreConsumer.validate(parsedBoxes.getFirst(), targetAsset);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String test = mapper.writeValueAsString(set);
        assertNotNull(test);
    }

    @Test
    void extractClaimV1() throws Exception {
        String asset = ResourceUtils.getFile("classpath:test-sample-standard-manifest.jpeg").getAbsolutePath();

        List<JumbfBox> rest = parser.parseMetadataFromFile(asset);

        TrustIndicatorSet set = manifestStoreConsumer.validate(rest.getFirst(), asset);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String test = mapper.writeValueAsString(set);
        assertNotNull(test);
    }

    private JumbfBox getManifestStore(String assetFileUrl) throws Exception {
        ActionsAssertion actions = new ActionsAssertion();

        ActionAssertion assertion1 = new ActionAssertion();
        assertion1.setAction(ActionChoice.C2PA_FILTERED.getValue());
        assertion1.setSoftwareAgent("Adobe Photoshop");
        assertion1.setWhen("22/1/22 10:12:32");

        actions.setActions(List.of(assertion1));

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        contentBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        final ThumbnailAssertion thumbnailAssertion = new ThumbnailAssertion();
        thumbnailAssertion.setThumbnailUrl(assetFileUrl);

        final ManifestBuilderV1 builder = new ManifestBuilderV1(new StandardManifestContentType());
        builder.addAssertion(actions);
        builder.addAssertion(thumbnailAssertion);
        builder.addAssertion(contentBindingAssertion);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setMediaType("image/jpeg");
        builder.setClaimSignatureCertificates(getCertificate());

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(builder.build());
        long totalBytesRequired = JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl, null);
        contentBindingAssertion.setDigest(digest);

        contentBindingAssertion.addExclusionRange((int) totalBytesRequired, 2);

        int paddingSize = calculateMinimumBytesRequired(2, (int) totalBytesRequired);
        pad = new byte[paddingSize];

        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        builder.removeAssertion("c2pa.hash.data");
        builder.addAssertion(contentBindingAssertion);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(
                getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath()));
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(signature.sign());

        return JpegTrustUtils.buildTrustRecord(builder.build());
    }

    private int calculateMinimumBytesRequired(int a, int b) throws Exception {
        CBORMapper mapper = new CBORMapper();
        int additionalBytesForA = mapper.writeValueAsBytes(a).length;
        int additionalBytesForB = mapper.writeValueAsBytes(b).length;
        return (CoreUtils.INT_BYTE_SIZE * 2) - additionalBytesForA - additionalBytesForB;
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
            String keyContent = new String(Files.readAllBytes(
                    Paths.get(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath())));
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC");

            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
