package org.mipams.jpegtrust.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.ExclusionRange;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.jpeg_systems.SaltHashBox;
import org.mipams.jpegtrust.jpeg_systems.content_types.ProvenanceContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.DescriptionBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.JumbfUriUtils;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public class JpegTrustUtils {

    public static BmffBox addSaltBytes() throws MipamsException {
        SaltHashBox saltHashBox = new SaltHashBox();
        saltHashBox.setContent(CoseUtils.getByteArray(32));
        saltHashBox.updateFieldsBasedOnExistingData();
        return saltHashBox;
    }

    public static long computeJpegTrustRecordSizeInBytes(JumbfBox... manifests)
            throws MipamsException {
        JumbfBoxBuilder manifestStore = new JumbfBoxBuilder(new TrustRecordContentType());
        manifestStore.setJumbfBoxAsRequestable();
        manifestStore.setLabel(new TrustRecordContentType().getLabel());

        manifestStore.appendAllContentBoxes(Arrays.asList(manifests));

        JumbfBox trustRecord = manifestStore.getResult();

        return trustRecord.getBoxSizeFromBmffHeaders();
    }

    public static byte[] computeSha256DigestOfFileContents(String filePath, List<ExclusionRange> exclusions)
            throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        List<ExclusionRange> sortedRanges = exclusions != null ? exclusions.stream()
                .sorted(Comparator.comparingLong(ExclusionRange::getStart))
                .toList() : List.of();

        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            long fileOffset = 0;
            int rangeIndex = 0;
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                int start = 0;

                while (rangeIndex < sortedRanges.size()) {
                    ExclusionRange r = sortedRanges.get(rangeIndex);
                    long rangeStart = r.getStart();
                    long rangeEnd = r.getStart() + r.getLength();

                    if (rangeEnd <= fileOffset) {
                        rangeIndex++;
                        continue;
                    }

                    if (rangeStart >= fileOffset + bytesRead) {
                        break;
                    }

                    int overlapStart = (int) Math.max(rangeStart - fileOffset, 0);
                    int overlapEnd = (int) Math.min(rangeEnd - fileOffset, bytesRead);

                    if (overlapStart > start) {
                        digest.update(buffer, start, overlapStart - start);
                    }

                    start = overlapEnd;

                    if (rangeEnd <= fileOffset + bytesRead) {
                        rangeIndex++;
                    } else {
                        break;
                    }
                }

                if (start < bytesRead) {
                    digest.update(buffer, start, bytesRead - start);
                }

                fileOffset += bytesRead;
            }
        }

        return digest.digest();
    }

    public static JumbfBox buildTrustRecord(JumbfBox... manifests) throws MipamsException {
        JumbfBoxBuilder manifestStore = new JumbfBoxBuilder(new TrustRecordContentType());
        manifestStore.setJumbfBoxAsRequestable();
        manifestStore.setLabel(new TrustRecordContentType().getLabel());

        manifestStore.appendAllContentBoxes(Arrays.asList(manifests));

        return manifestStore.getResult();
    }

    public static String locateActiveManifestUuid(JumbfBox manifestStoreJumbfBox) {
        JumbfBox manifestJumbfBox = locateActiveManifest(manifestStoreJumbfBox);
        return manifestJumbfBox.getDescriptionBox().getLabel();
    }

    public static JumbfBox locateActiveManifest(JumbfBox manifestStoreJumbfBox) {
        int manifestStoreSize = manifestStoreJumbfBox.getContentBoxList().size();
        return (JumbfBox) manifestStoreJumbfBox.getContentBoxList().get(manifestStoreSize - 1);
    }

    public static Optional<JumbfBox> locateManifestFromUri(JumbfBox manifestStoreJumbfBox,
            String targetManifestLabel) throws MipamsException {
        try {
            String targetManifestUri = String.format("%s/%s/%s", JumbfUriUtils.SELF_CONTAINED_URI,
                    manifestStoreJumbfBox.getDescriptionBox().getLabel(), targetManifestLabel);
            return JumbfUriUtils.getJumbfBoxFromAbsoluteUri(targetManifestUri, manifestStoreJumbfBox);
        } catch (MipamsException e) {
            return Optional.empty();
        }
    }

    public static String getProvenanceJumbfURL(String manifestId, String... childFieldList) {

        // String[] manifestIdParts = manifestId.split(":");
        // manifestIdParts[2] = manifestIdParts[2].toUpperCase();
        StringBuilder result = new StringBuilder("self#jumbf=/c2pa/").append(manifestId);

        for (String childField : childFieldList) {
            result.append("/").append(childField);
        }

        return result.toString();
    }

    public static byte[] computeSha256DigestOfFileContents(String filePath) throws MipamsException {

        try (InputStream fis = new FileInputStream(filePath)) {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[128];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sha.update(buffer, 0, bytesRead);
            }

            return sha.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new MipamsException(ValidationCode.ALGORITHM_UNSUPPORTED.getCode(), e);
        } catch (IOException e) {
            throw new MipamsException(ValidationCode.GENERAL_ERROR.getCode(), e);
        }
    }

    public static JumbfBox locateJpegTrustJumbfBoxByContentType(JumbfBox manifestJumbfBox,
            ProvenanceContentType contentType) throws MipamsException {

        JumbfBox result = null;

        for (BmffBox contentBox : manifestJumbfBox.getContentBoxList()) {
            JumbfBox jumbfBox = (JumbfBox) contentBox;

            if (contentType.getContentTypeUuid().equals(jumbfBox.getDescriptionBox().getUuid())) {
                result = jumbfBox;
            }
        }

        if (result == null) {
            throw new MipamsException(
                    String.format(ValidationCode.GENERAL_ERROR.getCode(), contentType.getLabel()));
        }

        return result;
    }

    public static Assertion deserializeCborJumbfBox(JumbfBox assertionJumbfBox,
            Class<? extends Assertion> assertionClass) throws MipamsException {

        CborBox assertionCborBox = (CborBox) assertionJumbfBox.getContentBoxList().get(0);

        ObjectMapper mapper = new CBORMapper();
        try {
            return mapper.readValue(new ByteArrayInputStream(assertionCborBox.getContent()),
                    assertionClass);
        } catch (IOException e) {
            throw new MipamsException(ValidationCode.ASSERTION_CBOR_INVALID.getCode(), e);
        }
    }

    public static byte[] calculateDigestForJumbfBox1(JumbfBox jumbfBox) throws MipamsException {
        String tempFilePath = "";
        try {

            String tempFile = CoreUtils.randomStringGenerator();
            tempFilePath = CoreUtils.createTempFile(tempFile, CoreUtils.JUMBF_FILENAME_SUFFIX);

            ApplicationContext context = new AnnotationConfigApplicationContext(
                    JpegTrustConfig.class, JumbfConfig.class);
            CoreGeneratorService coreGeneratorService = context.getBean(CoreGeneratorService.class);
            coreGeneratorService.generateJumbfMetadataToFile(List.of(jumbfBox), tempFilePath);

            ((ConfigurableApplicationContext) context).close();

            try (FileInputStream fis = new FileInputStream(tempFilePath)) {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");

                byte[] buffer = new byte[128];

                fis.skip((jumbfBox.isXBoxEnabled() ? 16 : 8));

                while (fis.available() > 0) {
                    int l = fis.read(buffer);
                    sha.update(buffer, 0, l);
                }

                return sha.digest();
            }
        } catch (Exception e) {
            throw new MipamsException(e);
        } finally {
            CoreUtils.deleteFile(tempFilePath);
        }
    }

    public static String getLabelFromManifestUri(String uri) {
        return uri.replace("self#jumbf=/c2pa/", "");
    }

    public static JumbfBox locateManifestOnManifestStore(String manifestUuid, JumbfBox manifestStoreJumbfBox)
            throws MipamsException {
        String manifestUri = String.format("%s/%s/%s", JumbfUriUtils.SELF_CONTAINED_URI,
                manifestStoreJumbfBox.getDescriptionBox().getLabel(), manifestUuid);
        return JumbfUriUtils.getJumbfBoxFromAbsoluteUri(manifestUri,
                manifestStoreJumbfBox).orElseThrow();
    }

    public static Map<String, Long> computeDuplicateLabelOccurrenceMap(List<JumbfBox> boxList) {
        return boxList.stream().map(box -> box.getDescriptionBox())
                .collect(Collectors.groupingBy(DescriptionBox::getLabel, Collectors.counting())).entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static String extractJumbfFragmentFromUri(String url) {
        String[] result = url.split("#jumbf=");

        if (result == null || result.length != 2) {
            return null;
        }

        return result[1];
    }

    public static long getSizeOfJumbfInApp11SegmentsInBytes(JumbfBox jumbfBox)
            throws MipamsException {
        long jumbfBoxSize = (jumbfBox.getBoxSizeFromBmffHeaders() > 0) ? jumbfBox.getBoxSizeFromBmffHeaders()
                : jumbfBox.getBoxSize();

        if (jumbfBoxSize == 0) {
            throw new MipamsException(
                    "Cannot know the size of JUMBF Box in advance. LBox equals to 0.");
        }

        long jumbfBoxHeaderSize = CoreUtils.INT_BYTE_SIZE * 2;
        jumbfBoxHeaderSize += jumbfBox.isXBoxEnabled() ? CoreUtils.LONG_BYTE_SIZE : 0;

        final int totalSegments = (int) Math.ceil((double) jumbfBoxSize / CoreUtils.MAX_APP_SEGMENT_SIZE);

        final int app11MarkerSize = CoreUtils.WORD_BYTE_SIZE;
        final int segmentLengthSize = CoreUtils.WORD_BYTE_SIZE;
        final int commonIdentifierSize = CoreUtils.WORD_BYTE_SIZE;
        final int boxInstanceNumberSize = CoreUtils.WORD_BYTE_SIZE;
        final int packetSequenceSize = CoreUtils.INT_BYTE_SIZE;

        final long jpegXtHeaderSize = app11MarkerSize + segmentLengthSize + commonIdentifierSize
                + boxInstanceNumberSize + packetSequenceSize + jumbfBoxHeaderSize;

        long totalApp11SegmentsSize = jpegXtHeaderSize - jumbfBoxHeaderSize + jpegXtHeaderSize * (totalSegments - 1);
        return totalApp11SegmentsSize + jumbfBoxSize;
    }

    public static byte[] encodeToDER(byte[] rawSignature) throws Exception {
        int len = rawSignature.length / 2;
        byte[] rBytes = new byte[len];
        byte[] sBytes = new byte[len];

        System.arraycopy(rawSignature, 0, rBytes, 0, len);
        System.arraycopy(rawSignature, len, sBytes, 0, len);

        BigInteger r = new BigInteger(1, rBytes);
        BigInteger s = new BigInteger(1, sBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x30);

        ByteArrayOutputStream content = new ByteArrayOutputStream();

        byte[] rEncoded = r.toByteArray();
        content.write(0x02);
        content.write(rEncoded.length);
        content.write(rEncoded);

        byte[] sEncoded = s.toByteArray();
        content.write(0x02);
        content.write(sEncoded.length);
        content.write(sEncoded);

        byte[] contentBytes = content.toByteArray();
        baos.write(contentBytes.length);
        baos.write(contentBytes);

        return baos.toByteArray();
    }
}
