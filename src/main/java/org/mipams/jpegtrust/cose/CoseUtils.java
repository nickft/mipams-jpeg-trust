package org.mipams.jpegtrust.cose;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.util.Arrays;
import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jumbf.util.MipamsException;

import com.authlete.cbor.CBORTaggedItem;
import com.authlete.cose.COSEProtectedHeader;
import com.authlete.cose.COSEProtectedHeaderBuilder;
import com.authlete.cose.COSESign1;
import com.authlete.cose.COSESign1Builder;
import com.authlete.cose.COSEUnprotectedHeader;
import com.authlete.cose.COSEUnprotectedHeaderBuilder;
import com.authlete.cose.SigStructure;
import com.authlete.cose.SigStructureBuilder;
import com.authlete.cose.constants.COSEAlgorithms;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public class CoseUtils {

    public static Claim toClaim(byte[] cborEncodedBytestream) throws StreamReadException, DatabindException, IOException {
        ObjectMapper mapper = new CBORMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(cborEncodedBytestream, Claim.class);
    }

    public static byte[] toCborEncodedByteArray(Object object) {
        try{
            ObjectMapper mapper = new CBORMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.setSerializationInclusion(Include.NON_EMPTY);

            return mapper.writeValueAsBytes(object);
        } catch (Exception e) {
            System.out.println("ERROR");
            return null;
        }
    }

    public static byte[] toSigStructure(Claim claim, List<X509Certificate> claimSignatureCertificates) throws CertificateEncodingException {

        byte[] payload = toCborEncodedByteArray(claim);

        COSEProtectedHeader protectedHeader = new COSEProtectedHeaderBuilder().alg(COSEAlgorithms.ES256).x5chain(claimSignatureCertificates).build();

        SigStructure structure = new SigStructureBuilder()
            .signature1()
            .bodyAttributes(protectedHeader)
            .payload(payload)
            .build();

        return structure.encode();
    }

    public static byte[] getByteArray(int numOfBytes) throws MipamsException {

        if (numOfBytes > 256) {
            throw new MipamsException("Random Generator supports size up to 256 Bytes");
        }

        SecureRandom randomNumberGenerator = new SecureRandom();
        return randomNumberGenerator.generateSeed(numOfBytes);
    }

    public static byte[] toCoseSign1EncodedBytestream(Claim claim, List<X509Certificate> claimSignatureCertificates, byte[] claimSignature) throws MipamsException {
        try{
            COSEProtectedHeader protectedHeader = new COSEProtectedHeaderBuilder().alg(COSEAlgorithms.ES256).x5chain(claimSignatureCertificates).build();

            final int defaultPadSize = 5*1024;
            int paddingSize = (claimSignature != null && claimSignature.length > 0) ? defaultPadSize - claimSignature.length - 1 : defaultPadSize;
            byte[] pad = new byte[paddingSize];
            Arrays.fill(pad, Byte.valueOf("0"));

            COSEUnprotectedHeader unprotectedHeader = new COSEUnprotectedHeaderBuilder().put("pad", pad).build();

            COSESign1 coseSign1 = new COSESign1Builder().protectedHeader(protectedHeader).unprotectedHeader(unprotectedHeader).signature(claimSignature).build();
            
            CBORTaggedItem item = new CBORTaggedItem(18, coseSign1);
            return item.encode();
        } catch (CertificateEncodingException e) {
            throw new MipamsException(e);
        }
    }

    public static BindingAssertion toBindingAsserion(byte[] content) throws StreamReadException, DatabindException, IOException {
        ObjectMapper mapper = new CBORMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(content, BindingAssertion.class);
    }
}
