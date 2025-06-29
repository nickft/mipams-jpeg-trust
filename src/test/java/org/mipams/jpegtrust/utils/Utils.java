package org.mipams.jpegtrust.utils;


import java.util.Arrays;

import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.services.JpegXLParser;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public class Utils {

    public static BindingAssertion getBindingAssertionForAsset(String assetFileUrl, long trustRecordRequiredBytes)
            throws Exception {

        final BindingAssertion contentBindingAssertion = new BindingAssertion();
        contentBindingAssertion.setAlgorithm("sha256");
        contentBindingAssertion.addExclusionRange(0, 0);

        byte[] digest = JpegTrustUtils.computeSha256DigestOfFileContents(assetFileUrl);
        contentBindingAssertion.setDigest(digest);

        int paddingSize = 6;

        int offsetOfExclusionRange = 0;

        if (assetFileUrl.endsWith("jxl")) {
            offsetOfExclusionRange = getJumbfOffsetInJxlAsset(assetFileUrl);

            paddingSize = Utils.calculateMinimumBytesRequired(getJumbfOffsetInJxlAsset(assetFileUrl),
                    (int) trustRecordRequiredBytes);

        } else if (assetFileUrl.endsWith("jp2")) {
            offsetOfExclusionRange = getJumbfOffsetInJp2Asset(assetFileUrl);

            paddingSize = Utils.calculateMinimumBytesRequired(Utils.getJumbfOffsetInJp2Asset(assetFileUrl),
                    (int) trustRecordRequiredBytes);
        } else {
            offsetOfExclusionRange = CoreUtils.WORD_BYTE_SIZE;

            paddingSize = calculateMinimumBytesRequired(2, (int) trustRecordRequiredBytes);
        }

        contentBindingAssertion.addExclusionRange((int) trustRecordRequiredBytes, offsetOfExclusionRange);

        byte[] pad = new byte[paddingSize];
        Arrays.fill(pad, Byte.parseByte("0"));
        contentBindingAssertion.setPadding(pad);

        return contentBindingAssertion;
    }

    public static int calculateMinimumBytesRequired(int i, int totalBytesRequired) throws Exception {
        CBORMapper mapper = new CBORMapper();
        int additionalBytesForA = mapper.writeValueAsBytes(i).length;
        int additionalBytesForB = mapper.writeValueAsBytes(totalBytesRequired).length;
        return (CoreUtils.INT_BYTE_SIZE * 2) - additionalBytesForA - additionalBytesForB;
    }

    public static int getJumbfOffsetInJp2Asset(String assetUrl) throws MipamsException {
        return (int) CoreUtils.getFileSizeFromPath(assetUrl);
    }

    public static int getJumbfOffsetInJxlAsset(String assetUrl) throws MipamsException {
        ApplicationContext context = new AnnotationConfigApplicationContext(JumbfConfig.class);
        JpegXLParser jXlParser = context.getBean(JpegXLParser.class);

        boolean levelBoxExists = jXlParser.assetHasLevelBox(assetUrl);

        ((ConfigurableApplicationContext) context).close();

        final int signatureBoxSizeInBytes = 12;
        final int fileTypeBoxSizeInBytes = 20;
        final int levelBoxSizeInBytes = 4;

        return !levelBoxExists ? signatureBoxSizeInBytes + fileTypeBoxSizeInBytes
                : signatureBoxSizeInBytes + fileTypeBoxSizeInBytes + levelBoxSizeInBytes;

    }

}
