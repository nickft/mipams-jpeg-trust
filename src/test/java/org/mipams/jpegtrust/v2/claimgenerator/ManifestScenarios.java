package org.mipams.jpegtrust.v2.claimgenerator;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mipams.jpegtrust.builders.ManifestBuilder;
import org.mipams.jpegtrust.entities.DigestResultForJumbfBox;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.assertions.ingredients.StatusCodesMap;
import org.mipams.jpegtrust.entities.assertions.ingredients.StatusMap;
import org.mipams.jpegtrust.entities.assertions.ingredients.ValidationResultsMap;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimSignatureContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustDeclarationContentType;
import org.mipams.jpegtrust.services.JumbfBoxDigestService;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.utils.CryptoUtils;
import org.mipams.jpegtrust.utils.Utils;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.util.ResourceUtils;

public class ManifestScenarios {

    JumbfBoxDigestService jumbfBoxDigestService;

    public void setJumbfBoxDigestService(JumbfBoxDigestService jumbfBoxDigestService) {
        this.jumbfBoxDigestService = jumbfBoxDigestService;
    }

    public JumbfBox getManifestWithAssertions(String assetFileUrl, String mediaType, List<Assertion> assertions,
            JumbfBox... manifests) throws Exception {

        List<JumbfBox> assertionJumbfBoxes = new ArrayList<>();

        for (Assertion assertion : assertions) {
            assertionJumbfBoxes.add(assertion.toJumbfBox());
        }

        return getManifestWithAssertionBoxes(assetFileUrl, mediaType, assertionJumbfBoxes, manifests);
    }

    public JumbfBox getManifestWithAssertionBoxes(String assetFileUrl, String mediaType, List<JumbfBox> assertions,
            JumbfBox... manifests) throws Exception {

        final ManifestBuilder builder = new ManifestBuilder(new StandardManifestContentType());

        for (JumbfBox assertionBox : assertions) {
            DigestResultForJumbfBox digestResult = jumbfBoxDigestService
                    .calculateDigestForJumbfBox(assertionBox);

            builder.addCreatedAssertion(assertionBox, digestResult);
        }

        final BindingAssertion tempBindingAssertion = new BindingAssertion();
        tempBindingAssertion.setAlgorithm("sha256");
        tempBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        tempBindingAssertion.setPadding(pad);

        JumbfBox tempBindingAssertionBox = tempBindingAssertion.toJumbfBox();
        DigestResultForJumbfBox tempResult = jumbfBoxDigestService
                .calculateDigestForJumbfBox(tempBindingAssertionBox);

        builder.addCreatedAssertion(tempBindingAssertionBox, tempResult);

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setAlgorithm("sha256");

        List<X509Certificate> certificates = CryptoUtils.getCertificate();
        builder.setClaimSignatureCertificates(certificates);

        JumbfBox activeManifest = builder.build();
        JumbfBox[] all = Stream
                .concat(Arrays.stream(manifests), Stream.of(activeManifest))
                .toArray(JumbfBox[]::new);

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(all);
        long totalBytesRequired = (mediaType.endsWith("jxl") || assetFileUrl.endsWith("jp2"))
                ? tempTrustRecord.getBoxSizeFromBmffHeaders()
                : JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        BindingAssertion contentBindingAssertion = Utils.getBindingAssertionForAsset(assetFileUrl,
                totalBytesRequired);

        builder.removeCreatedAssertion(AssertionDiscovery.MipamsAssertion.CONTENT_BINDING.getBaseLabel());

        JumbfBox contentBindingAssertionBox = contentBindingAssertion.toJumbfBox();
        DigestResultForJumbfBox contentBindingAssertionResult = jumbfBoxDigestService
                .calculateDigestForJumbfBox(contentBindingAssertionBox);

        builder.addCreatedAssertion(contentBindingAssertionBox, contentBindingAssertionResult);

        PrivateKey privKey = CryptoUtils
                .getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        byte[] payload = builder.encodeClaimToBeSigned();

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(payload);

        builder.setClaimSignature(CryptoUtils.decodeFromDER(signature.sign(), 32));

        return builder.build();
    }

    public JumbfBox getTrustDeclarationManifest(String assetFileUrl, String mediaType) throws Exception {
        ActionsAssertion actions = new ActionsAssertion();
        ActionAssertion assertion1 = new ActionAssertion();
        assertion1.setAction(ActionChoice.C2PA_CREATED.getValue());
        assertion1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        assertion1.setDigitalSourceType(
                "http://cv.iptc.org/newscodes/digitalsourcetype/algorithmicallyEnhanced");
        actions.setActions(List.of(assertion1));

        final BindingAssertion tempBindingAssertion = new BindingAssertion();
        tempBindingAssertion.setAlgorithm("sha256");
        tempBindingAssertion.addExclusionRange(0, 0);
        byte[] pad = new byte[6];
        Arrays.fill(pad, Byte.parseByte("0"));
        tempBindingAssertion.setPadding(pad);

        final ManifestBuilder builder = new ManifestBuilder(new TrustDeclarationContentType());

        for (JumbfBox assertionBox : List.of(actions.toJumbfBox(), tempBindingAssertion.toJumbfBox())) {
            DigestResultForJumbfBox digestResult = jumbfBoxDigestService
                    .calculateDigestForJumbfBox(assertionBox);

            builder.addCreatedAssertion(assertionBox, digestResult);
        }

        builder.setTitle("MIPAMS test image");
        builder.setInstanceID("uuid:7b57930e-2f23-47fc-affe-0400d70b738d");
        builder.setGeneratorInfoName("MIPAMS GENERATOR 0.1");
        builder.setAlgorithm("sha256");

        List<X509Certificate> certificates = CryptoUtils.getCertificate();
        builder.setClaimSignatureCertificates(certificates);

        JumbfBox tempTrustRecord = JpegTrustUtils.buildTrustRecord(builder.build());
        long totalBytesRequired = (mediaType.endsWith("jxl") || assetFileUrl.endsWith("jp2"))
                ? tempTrustRecord.getBoxSizeFromBmffHeaders()
                : JpegTrustUtils.getSizeOfJumbfInApp11SegmentsInBytes(tempTrustRecord);

        BindingAssertion contentBindingAssertion = Utils.getBindingAssertionForAsset(assetFileUrl,
                totalBytesRequired);

        builder.removeCreatedAssertion(AssertionDiscovery.MipamsAssertion.CONTENT_BINDING.getBaseLabel());

        JumbfBox contentBindingBox = contentBindingAssertion.toJumbfBox();

        DigestResultForJumbfBox digestResult = jumbfBoxDigestService
                .calculateDigestForJumbfBox(contentBindingBox);

        builder.addCreatedAssertion(contentBindingBox, digestResult);

        PrivateKey privKey = CryptoUtils
                .getPrivateKey(ResourceUtils.getFile("classpath:privKey.pem").getAbsolutePath());

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privKey);
        signature.update(builder.encodeClaimToBeSigned());
        builder.setClaimSignature(CryptoUtils.decodeFromDER(signature.sign(), 32));

        return builder.build();
    }

    public static IngredientAssertion getIngredientAssertion(JumbfBox ingredientManifest,
            JumbfBoxDigestService digestService, String mediaType)
            throws MipamsException {
        IngredientAssertion ingredientAssertion = new IngredientAssertion();
        ingredientAssertion.setRelationship(IngredientAssertionV1.RELATIONSHIP_PARENT_OF);
        ingredientAssertion.setInstanceId("ab610ae5124002be3dbf0c589a2f1f");
        ingredientAssertion.setTitle("Ingredient manifest");
        ingredientAssertion.setMediaType(mediaType);

        DigestResultForJumbfBox locallyComputedHash = digestService
                .calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference
                .setUrl(String.format("self#jumbf=/c2pa/%s",
                        ingredientManifest.getDescriptionBox().getLabel()));
        hashedUriReference.setDigest(locallyComputedHash.getDigest());

        ingredientAssertion.setActiveManifestOfIngredient(hashedUriReference);

        JumbfBox claimSignature = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(ingredientManifest,
                new ClaimSignatureContentType());

        DigestResultForJumbfBox locallyComputedHashForClaimSignature = digestService
                .calculateDigestForJumbfBox(claimSignature);
        final HashedUriReference hashedUriReferenceForClaimSignature = new HashedUriReference();
        hashedUriReferenceForClaimSignature
                .setUrl(String.format("self#jumbf=/c2pa/%s/%s",
                        ingredientManifest.getDescriptionBox().getLabel(),
                        (new ClaimSignatureContentType()).getLabel()));
        hashedUriReferenceForClaimSignature.setDigest(locallyComputedHashForClaimSignature.getDigest());

        ingredientAssertion.setClaimSignatureOfIngredient(hashedUriReferenceForClaimSignature);

        ValidationResultsMap validationResultsMap = new ValidationResultsMap();

        StatusMap status1 = new StatusMap();
        status1.setCode(ValidationCode.CLAIM_SIGNATURE_VALIDATED.getCode());

        StatusMap status2 = new StatusMap();
        status2.setCode(ValidationCode.ASSERTION_DATA_HASH_MATCH.getCode());

        StatusCodesMap statusCodeMap = new StatusCodesMap();
        statusCodeMap.setSuccess(
                List.of(status1, status2));

        validationResultsMap.setActiveManifest(statusCodeMap);

        ingredientAssertion.setValidationResults(validationResultsMap);

        return ingredientAssertion;
    }

    public static Assertion getInitialActions() {
        ActionAssertion action1 = new ActionAssertion();
        action1.setAction(ActionChoice.C2PA_CREATED.getValue());
        action1.setSoftwareAgent("Image Editing Tool");
        action1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        action1.setParameters(Map.of("instanceID", "xmp:iid:e928fac1-8473-4c70-1982-369e91d4e58d"));

        ActionAssertion action2 = new ActionAssertion();
        action2.setAction(ActionChoice.C2PA_CONVERTED.getValue());
        action2.setSoftwareAgent("Image Editing Tool");
        action2.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        action2.setParameters(Map.of("instanceID", "xmp:iid:e928fac1-8473-4c70-1982-369e91d4e58d"));

        ActionsAssertion actions = new ActionsAssertion();
        actions.setActions(List.of(action1, action2));

        return actions;
    }

    public static Assertion getActionsWithIngredientAssertion(JumbfBox ingredientManifest,
            JumbfBoxDigestService jumbfBoxDigestService) throws MipamsException {

        ActionAssertion action1 = new ActionAssertion();
        action1.setAction(ActionChoice.C2PA_OPENED.getValue());
        action1.setSoftwareAgent("Image Editing Tool");
        action1.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        final DigestResultForJumbfBox locallyComputedHash = jumbfBoxDigestService
                .calculateDigestForJumbfBox(ingredientManifest);
        final HashedUriReference hashedUriReference = new HashedUriReference();
        hashedUriReference
                .setUrl(String.format("self#jumbf=c2pa.assertions/c2pa.ingredient.v3"));
        hashedUriReference.setAlgorithm(locallyComputedHash.getAlgorithm());
        hashedUriReference.setDigest(locallyComputedHash.getDigest());

        action1.setIngredients(List.of(hashedUriReference));

        ActionAssertion action2 = new ActionAssertion();
        action2.setAction(ActionChoice.C2PA_FILTERED.getValue());
        action2.setSoftwareAgent("Image Editing Tool");
        action2.setWhen(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        ActionsAssertion actions = new ActionsAssertion();
        actions.setActions(List.of(action1, action2));

        return actions;
    }

}
