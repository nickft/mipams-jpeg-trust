package org.mipams.jpegtrust.utils;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.ResourceUtils;

public class CryptoUtils {

    public static List<X509Certificate> getCertificate() throws Exception {
        try (FileInputStream fis = new FileInputStream(
                ResourceUtils.getFile("classpath:certs.pem").getAbsolutePath())) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificates(fis).stream().map(cert -> (X509Certificate) cert)
                    .collect(Collectors.toList());
        }
    }

    public static PrivateKey getPrivateKey(String absolutePath) throws Exception {
        try {
            String keyContent = new String(Files.readAllBytes(
                    Paths.get(absolutePath)));
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC");

            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static byte[] decodeFromDER(byte[] derSignature, int keyLength) throws Exception {
        if (derSignature[0] != 0x30)
            throw new IllegalArgumentException("Invalid DER format");

        int offset = 2;
        if (derSignature[1] > 0x80) {
            int lengthBytes = derSignature[1] & 0x7F;
            offset = 2 + lengthBytes;
        }

        if (derSignature[offset] != 0x02)
            throw new IllegalArgumentException("Invalid DER INTEGER for r");
        int rLength = derSignature[offset + 1];
        byte[] rBytes = new byte[keyLength];
        System.arraycopy(derSignature, offset + 2 + Math.max(0, rLength - keyLength),
                rBytes, Math.max(0, keyLength - rLength),
                Math.min(rLength, keyLength));

        int sOffset = offset + 2 + rLength;
        if (derSignature[sOffset] != 0x02)
            throw new IllegalArgumentException("Invalid DER INTEGER for s");
        int sLength = derSignature[sOffset + 1];
        byte[] sBytes = new byte[keyLength];
        System.arraycopy(derSignature, sOffset + 2 + Math.max(0, sLength - keyLength),
                sBytes, Math.max(0, keyLength - sLength),
                Math.min(sLength, keyLength));

        byte[] rawSignature = new byte[2 * keyLength];
        System.arraycopy(rBytes, 0, rawSignature, 0, keyLength);
        System.arraycopy(sBytes, 0, rawSignature, keyLength, keyLength);

        return rawSignature;
    }

}
