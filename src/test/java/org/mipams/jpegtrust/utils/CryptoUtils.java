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

}
