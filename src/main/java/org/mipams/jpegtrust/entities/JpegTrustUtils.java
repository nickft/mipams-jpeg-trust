package org.mipams.jpegtrust.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.config.JpegTrustConfig;
import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.ExclusionRange;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.jpeg_systems.JumbfUtils;
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

    public static byte[] computeSha256DigestOfFileContents(String filePath, List<ExclusionRange> ex)
            throws MipamsException {
        if (ex == null || ex.isEmpty()) {
            return computeSha256DigestOfFileContents(filePath);
        }

        List<ExclusionRange> sortedExclusionRanges = ex.stream()
                .sorted(Comparator.comparingInt(ExclusionRange::getStart))
                .collect(Collectors.toList());
        int iteratorIndex = 0;
        Iterator<ExclusionRange> exclusionRangeIterator = sortedExclusionRanges.stream().iterator();

        String tempFile = CoreUtils.randomStringGenerator();
        String tempFilePath = CoreUtils.createTempFile(tempFile, CoreUtils.JUMBF_FILENAME_SUFFIX);
        try (FileInputStream fis = new FileInputStream(filePath)) {

            try (FileOutputStream outputStream = new FileOutputStream(tempFilePath)) {
                byte[] buffer = new byte[128];

                Optional<ExclusionRange> range = getNextEligibleExclusionRange(exclusionRangeIterator, iteratorIndex);

                while (fis.available() > 0) {
                    int l = fis.read(buffer);

                    byte[] eligibleBytes = filterExclusionRangeBytes(buffer, range, iteratorIndex, l);
                    iteratorIndex += l;

                    if (eligibleBytes.length > 0) {
                        outputStream.write(eligibleBytes);
                    }

                    if (range.isPresent() && isExlusionRangeObsolete(range.get(), iteratorIndex)) {
                        range = getNextEligibleExclusionRange(exclusionRangeIterator, iteratorIndex);
                    }
                }
            }

            return computeSha256DigestOfFileContents(tempFilePath);
        } catch (Exception e) {
            throw new MipamsException(e);
        } finally {
            CoreUtils.deleteFile(tempFilePath);
        }
    }

    private static Optional<ExclusionRange> getNextEligibleExclusionRange(
            Iterator<ExclusionRange> exclusionRangeIterator, long iteratorIndex) {
        ExclusionRange range = null;
        while (exclusionRangeIterator.hasNext()) {
            range = exclusionRangeIterator.next();

            if (!isExlusionRangeObsolete(range, iteratorIndex)) {
                return Optional.of(range);
            }
        }

        return Optional.empty();
    }

    private static boolean isExlusionRangeObsolete(ExclusionRange range, long iteratorIndex) {
        return range.getStart() + range.getLength() < iteratorIndex;
    }

    private static byte[] filterExclusionRangeBytes(byte[] buffer, Optional<ExclusionRange> range,
            int absoluteStartIndex, int size) throws Exception {
        if (range.isEmpty()) {
            byte[] resultBuffer = new byte[size];
            System.arraycopy(buffer, 0, resultBuffer, 0, size);
            return resultBuffer;
        }

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();) {
            for (int i = 0; i < size; i++) {

                if (absoluteStartIndex + i >= range.get().getStart() && absoluteStartIndex
                        + i < range.get().getStart() + range.get().getLength()) {
                    continue;
                }

                byteStream.write(buffer[i]);
            }

            return byteStream.toByteArray();
        }
    }

    public static String getClaimContentBase64(JumbfBox trustRecord) throws MipamsException {
        Optional<JumbfBox> jumbfBoxClaim = JumbfUtils.searchJumbfBox(
                (JumbfBox) trustRecord.getContentBoxList().getFirst(), "self#jumbf=c2pa.claim");
        final CborBox contentBox = (CborBox) jumbfBoxClaim.get().getContentBoxList().getFirst();

        return Base64.getEncoder().encodeToString(contentBox.getContent());
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
            String targetManifestUri) throws MipamsException {
        try {
            return Optional
                    .of(CoreUtils.locateJumbfBoxFromLabel(
                            manifestStoreJumbfBox.getContentBoxList().stream()
                                    .map(box -> (JumbfBox) box).collect(Collectors.toList()),
                            targetManifestUri));
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

    public static byte[] calculateDigestForJumbfBox(JumbfBox jumbfBox) throws MipamsException {
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
        return CoreUtils.locateJumbfBoxFromLabel(
                manifestStoreJumbfBox.getContentBoxList().stream()
                        .map(box -> (JumbfBox) box).collect(Collectors.toList()),
                manifestUuid);
    }

    public static Map<String, Long> computeDuplicateLabelOccurrenceMap(List<JumbfBox> boxList) {
        return boxList.stream().map(box -> box.getDescriptionBox())
                .collect(Collectors.groupingBy(DescriptionBox::getLabel, Collectors.counting())).entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
