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
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimSignatureIndicators;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.authlete.cose.COSEProtectedHeader;
import com.authlete.cose.COSEUnprotectedHeader;
import com.authlete.cose.constants.COSEAlgorithms;

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

    public COSEProtectedHeader getClaimSignatureProtectedHeaderFromClaimSignatureBox(JumbfBox claimSignatureJumbfBox)
            throws MipamsException {

        CborBox claimSignatureContent = (CborBox) claimSignatureJumbfBox.getContentBoxList().get(0);
        return CoseUtils.extractProtectedHeaderFromCoseSign1(claimSignatureContent.getContent());
    }

    public COSEUnprotectedHeader getClaimSignatureUnprotectedHeaderFromClaimSignatureBox(
            JumbfBox claimSignatureJumbfBox)
            throws MipamsException {

        CborBox claimSignatureContent = (CborBox) claimSignatureJumbfBox.getContentBoxList().get(0);
        return CoseUtils.extractUnprotectedHeaderFromCoseSign1(claimSignatureContent.getContent());
    }

    public List<X509Certificate> getListOfCertificatesFromClaimSignatureBox(
            JumbfBox claimSignatureJumbfBox) throws MipamsException {

        CborBox claimSignatureContent = (CborBox) claimSignatureJumbfBox.getContentBoxList().get(0);
        return CoseUtils.extractCertificatesFromCoseSign1(claimSignatureContent.getContent());

    }

    public void validateSignature(JumbfBox claimSignatureJumbfBox, JumbfBox claimJumbfBox)
            throws MipamsException {
        try {
            byte[] claimSignature = getClaimSignatureFromClaimSignatureBox(claimSignatureJumbfBox);

            List<X509Certificate> certificates = getListOfCertificatesFromClaimSignatureBox(claimSignatureJumbfBox);

            byte[] cborClaimPayload = ((CborBox) claimJumbfBox.getContentBoxList().get(0)).getContent();

            COSEProtectedHeader protectedHeader = getClaimSignatureProtectedHeaderFromClaimSignatureBox(
                    claimSignatureJumbfBox);

            byte[] encodedClaim = CoseUtils.produceCOSESigStructureFromTrustManifest(cborClaimPayload, protectedHeader);

            String publicKeyString = Base64.getEncoder()
                    .encodeToString(certificates.get(0).getPublicKey().getEncoded());

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            PublicKey pubKey;
            Signature signature;
            byte[] signaturePayload = claimSignature;

            Object algObj = protectedHeader.getAlg();

            if (algObj == null) {
                throw new MipamsException("COSE Protected Header is missing the algorithm (alg) parameter.");
            }

            int coseAlgorithmId = CoseUtils.getSigningAlgorithmFromCOSEProtectedHeader(protectedHeader);

            switch (coseAlgorithmId) {
                // --- ECDSA Family ---
                case COSEAlgorithms.ES256: // -7
                    pubKey = KeyFactory.getInstance("EC").generatePublic(keySpec);
                    signature = Signature.getInstance("SHA256withECDSA");
                    signaturePayload = JpegTrustUtils.encodeToDER(claimSignature);
                    break;
                case COSEAlgorithms.ES384: // -35
                    pubKey = KeyFactory.getInstance("EC").generatePublic(keySpec);
                    signature = Signature.getInstance("SHA384withECDSA");
                    signaturePayload = JpegTrustUtils.encodeToDER(claimSignature);
                    break;
                case COSEAlgorithms.ES512: // -36
                    pubKey = KeyFactory.getInstance("EC").generatePublic(keySpec);
                    signature = Signature.getInstance("SHA512withECDSA");
                    signaturePayload = JpegTrustUtils.encodeToDER(claimSignature);
                    break;

                // --- RSA-PSS Family ---
                case COSEAlgorithms.PS256: // -37
                    pubKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                    signature = Signature.getInstance("RSASSA-PSS");
                    signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
                    break;
                case COSEAlgorithms.PS384: // -38
                    pubKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                    signature = Signature.getInstance("RSASSA-PSS");
                    signature.setParameter(new PSSParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384, 48, 1));
                    break;
                case COSEAlgorithms.PS512: // -39
                    pubKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                    signature = Signature.getInstance("RSASSA-PSS");
                    signature.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
                    break;

                // --- EdDSA Family ---
                case COSEAlgorithms.EdDSA: // -8
                    // Note: Use "Ed25519" or "EdDSA" depending on your provider (Java 15+)
                    pubKey = KeyFactory.getInstance("EdDSA").generatePublic(keySpec);
                    signature = Signature.getInstance("EdDSA");
                    // EdDSA signatures are already "plain" fixed-length, no DER conversion needed
                    break;

                default:
                    throw new MipamsException("Unsupported COSE algorithm ID: " + coseAlgorithmId);
            }

            signature.initVerify(pubKey);
            signature.update(encodedClaim);
            boolean isValid = signature.verify(signaturePayload);

            if (!isValid) {
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

        COSEProtectedHeader protectedHeader = getClaimSignatureProtectedHeaderFromClaimSignatureBox(
                claimSignatureJumbfBox);

        int coseAlgorithmId = CoseUtils.getSigningAlgorithmFromCOSEProtectedHeader(protectedHeader);

        claimSignatureIndicators.setSignatureAlgorithm(COSEAlgorithms.getNameByValue(coseAlgorithmId));

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
        String[] dnParts = principal.getName().split(",\s*");

        for (String part : dnParts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                subjectMap.put(keyValue[0], keyValue[1]);
            }
        }
        return subjectMap;
    }
}
