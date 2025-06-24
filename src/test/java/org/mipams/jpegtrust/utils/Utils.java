package org.mipams.jpegtrust.utils;

import org.mipams.jumbf.util.CoreUtils;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public class Utils {

    public static int calculateMinimumBytesRequired(int i, int totalBytesRequired) throws Exception {
        CBORMapper mapper = new CBORMapper();
        int additionalBytesForA = mapper.writeValueAsBytes(i).length;
        int additionalBytesForB = mapper.writeValueAsBytes(totalBytesRequired).length;
        return (CoreUtils.INT_BYTE_SIZE * 2) - additionalBytesForA - additionalBytesForB;
    }

}
