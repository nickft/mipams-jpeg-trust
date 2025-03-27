package org.mipams.jpegtrust.services.validation.consumer;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.ProvenanceEntity;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimSignatureIndicators;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClaimSignatureConsumer {

    @Autowired
    ClaimConsumer claimConsumer;

    public byte[] getClaimSignatureFromClaimSignatureBox(JumbfBox claimSignatureJumbfBox)
            throws MipamsException {

        CborBox claimSignatureContent = (CborBox) claimSignatureJumbfBox.getContentBoxList().get(0);
        byte[] claimSignature = CoseUtils.extractSignatureFromCoseSign1(claimSignatureContent.getContent());

        return claimSignature;
    }

    public List<X509Certificate> getListOfCertificatesFromClaimSignatureBox(
            JumbfBox claimSignatureJumbfBox) throws MipamsException {

        CborBox claimSignatureContent = (CborBox) claimSignatureJumbfBox.getContentBoxList().get(0);
        return CoseUtils.extractCertificatesFromCoseSign1(claimSignatureContent.getContent());

    }

    public void validateSignature(JumbfBox claimSignatureJumbfBox, ProvenanceEntity claim)
            throws MipamsException {
        try {
            byte[] claimSignature = getClaimSignatureFromClaimSignatureBox(claimSignatureJumbfBox);
            List<X509Certificate> certificates = getListOfCertificatesFromClaimSignatureBox(claimSignatureJumbfBox);

            byte[] encodedClaim = CoseUtils.toSigStructure(claim, certificates);

            String publicKeyString = Base64.getEncoder()
                    .encodeToString(certificates.get(0).getPublicKey().getEncoded());

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(pubKey);
            signature.update(encodedClaim);

            boolean result = signature.verify(claimSignature);

            if (!result) {
                throw new ValidationException(ValidationCode.SIGNING_CREDENTIAL_INVALID);
            }

            if (certificates.size() > 1) {
                X509Certificate intermediateCert = certificates.get(certificates.size() - 1);

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                CertPath certPath = cf.generateCertPath(List.of(certificates.get(0)));

                TrustAnchor trustAnchor = new TrustAnchor(intermediateCert, null);
                PKIXParameters params = new PKIXParameters(Collections.singleton(trustAnchor));
                params.setRevocationEnabled(false);

                CertPathValidator validator = CertPathValidator.getInstance("PKIX");

                try {
                    validator.validate(certPath, params);
                } catch (CertPathValidatorException e) {
                    System.out.println("Certificate chain validation failed: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new MipamsException(e);
        }
    }

    public ClaimSignatureIndicators buildClaimSignatureIndicatorSet(JumbfBox claimSignatureJumbfBox)
            throws MipamsException {

        List<X509Certificate> certificates = getListOfCertificatesFromClaimSignatureBox(claimSignatureJumbfBox);

        X509Certificate primaryCert = certificates.get(0);

        ClaimSignatureIndicators claimSignatureIndicators = new ClaimSignatureIndicators();

        claimSignatureIndicators.setSignatureAlgorithm(primaryCert.getSigAlgName());

        claimSignatureIndicators.getSubject().putAll(parseX500Principal(primaryCert.getSubjectX500Principal()));

        claimSignatureIndicators.getIssuer().putAll(parseX500Principal(primaryCert.getIssuerX500Principal()));

        claimSignatureIndicators.getValidity().put("not_before",
                DateTimeFormatter.ISO_INSTANT.format(primaryCert.getNotBefore().toInstant()));
        claimSignatureIndicators.getValidity().put("not_after",
                DateTimeFormatter.ISO_INSTANT.format(primaryCert.getNotAfter().toInstant()));

        return claimSignatureIndicators;
    }

    private static Map<String, String> parseX500Principal(X500Principal principal) {
        Map<String, String> subjectMap = new LinkedHashMap<>();
        String[] dnParts = principal.getName().split(",\\s*");

        for (String part : dnParts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                subjectMap.put(keyValue[0], keyValue[1]);
            }
        }
        return subjectMap;
    }
}
