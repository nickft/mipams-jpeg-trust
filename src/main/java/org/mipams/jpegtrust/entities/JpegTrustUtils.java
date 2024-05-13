package org.mipams.jpegtrust.entities;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.assertions.ExclusionRange;
import org.mipams.jpegtrust.jpeg_systems.JumbfUtils;
import org.mipams.jpegtrust.jpeg_systems.SaltHashBox;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustRecordContentType;
import org.mipams.jumbf.entities.BmffBox;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.util.MipamsException;

public class JpegTrustUtils {
    
    public static BmffBox addSaltBytes() throws MipamsException {
        SaltHashBox saltHashBox = new SaltHashBox();
        saltHashBox.setContent(CoseUtils.getByteArray(32));
        saltHashBox.updateFieldsBasedOnExistingData();
        return saltHashBox;
    }

    public static long computeJpegTrustRecordSizeInBytes(JumbfBox... manifests) throws MipamsException {
        JumbfBoxBuilder manifestStore = new JumbfBoxBuilder(new TrustRecordContentType());
        manifestStore.setJumbfBoxAsRequestable();
        manifestStore.setLabel(new TrustRecordContentType().getLabel());

        manifestStore.appendAllContentBoxes(Arrays.asList(manifests));

        JumbfBox trustRecord = manifestStore.getResult();

        return trustRecord.getBoxSizeFromBmffHeaders();
    }

        public static byte[] computeSha256DigestOfFileContents(String filePath, List<ExclusionRange> ex) throws Exception {
        if(ex == null || ex.isEmpty()) {
            return computeSha256DigestOfFileContents(filePath);
        }


        List<ExclusionRange> sortedExclusionRanges = ex.stream().sorted(Comparator.comparingInt(ExclusionRange::getStart)).collect(Collectors.toList());
        int iteratorIndex = 0;
        Iterator<ExclusionRange> exclusionRangeIterator = sortedExclusionRanges.stream().iterator();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[128]; 
    
            Optional<ExclusionRange> range = getNextEligibleExclusionRange(exclusionRangeIterator, iteratorIndex);

            while (fis.available() > 0) {
                int l = fis.read(buffer);

                byte[] eligibleBytes = filterExclusionRangeBytes(buffer, range, iteratorIndex, l);
                iteratorIndex +=l;

                if(eligibleBytes.length > 0) {
                    sha.update(eligibleBytes);
                }

                if(range.isPresent() && isExlusionRangeObsolete(range.get(), iteratorIndex)){
                    range = getNextEligibleExclusionRange(exclusionRangeIterator, iteratorIndex);    
                }
            }


            return sha.digest();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private static Optional<ExclusionRange> getNextEligibleExclusionRange(Iterator<ExclusionRange> exclusionRangeIterator, long iteratorIndex) {
        ExclusionRange range = null;
        while(exclusionRangeIterator.hasNext()) {
            range = exclusionRangeIterator.next();

            if(!isExlusionRangeObsolete(range, iteratorIndex)) {
                return Optional.of(range);
            }
        }

        return Optional.empty();
    }

    private static boolean isExlusionRangeObsolete(ExclusionRange range, long iteratorIndex) {
        return range.getStart() + range.getLength() < iteratorIndex;
    }

    private static byte[] filterExclusionRangeBytes(byte[] buffer, Optional<ExclusionRange> range, int absoluteStartIndex, int size) throws Exception {
        if(range.isEmpty()) {
            byte[] resultBuffer = new byte[size];
            System.arraycopy(buffer, 0, resultBuffer, 0, size);
            return resultBuffer;
        }

        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream();){
            for (int i = 0; i < size; i++) {
                
                if(absoluteStartIndex + i >= range.get().getStart() && absoluteStartIndex + i < range.get().getStart() + range.get().getLength()) {
                    continue;
                }

                byteStream.write(buffer[i]);
            }

            return byteStream.toByteArray();
        }
    }

    public static byte[] computeSha256DigestOfFileContents(String filePath) throws MipamsException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[128]; 
    
            while (fis.available() > 0) {
                int l = fis.read(buffer);
                sha.update(buffer, 0, l);
            }


            return sha.digest();
        } catch (Exception e) {
            throw new MipamsException(e);
        }
    }

    public static String getClaimContentBase64(JumbfBox trustRecord) throws MipamsException {
        Optional<JumbfBox> jumbfBoxClaim = JumbfUtils.searchJumbfBox((JumbfBox) trustRecord.getContentBoxList().getFirst(), "self#jumbf=c2pa.claim");
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
}
