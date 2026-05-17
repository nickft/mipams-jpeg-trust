package org.mipams.jpegtrust.services.validation.discovery;

import java.util.Optional;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertionBMFF;
import org.mipams.jpegtrust.entities.assertions.ThumbnailAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertionV1;
import org.mipams.jpegtrust.entities.assertions.cawg.IdentityAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV2;
import org.mipams.jpegtrust.entities.assertions.metadata.MetadataAssertion;
import org.mipams.jpegtrust.entities.assertions.tfm.AiDisclosureAssertion;
import org.mipams.jpegtrust.entities.assertions.tfm.EmbeddedDataAssertion;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.EmbeddedFileDescriptionBox;
import org.mipams.jumbf.entities.JsonBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.content_types.CborContentType;
import org.mipams.jumbf.services.content_types.EmbeddedFileContentType;
import org.mipams.jumbf.services.content_types.JsonContentType;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class AssertionDiscovery {

    public enum MipamsAssertion {

        CONTENT_BINDING("c2pa.hash.data"),
        CONTENT_BINDING_BMFF("c2pa.hash.bmff.v3"),
        SOFT_BINDING("c2pa.soft-binding", true),
        ACTION("c2pa.actions.v2"),
        ACTION_V1("c2pa.actions"),
        CAWG_IDENTIY("cawg.identity", true),
        CAWG_METADATA("cawg.metadata", true),
        INGREDIENT("c2pa.ingredient.v3", true),
        INGREDIENT_V2("c2pa.ingredient.v2", true),
        INGREDIENT_V1("c2pa.ingredient", true),
        EMBEDDED_DATA("c2pa.embedded-data", true),
        AI_DISCLOSURE("c2pa.ai-disclosure", true),
        METADATA("c2pa.metadata", true),
        CLAIM_THUMBNAIL("c2pa.thumbnail.claim", true),
        INGREDIENT_THUMBNAIL("c2pa.thumbnail.ingredient", true),
        JPT_EXTENT_OF_MODIFICATION("jpt.mod-extent", true),
        JPT_RIGHTS("jpt.rights", true);


        private String baseLabel;
        private boolean isRedactable = false;

        MipamsAssertion(String field, boolean isRedactable) {
            this.baseLabel = field;
            this.isRedactable = isRedactable;
        }

        MipamsAssertion(String field) {
            this.baseLabel = field;
        }

        public static MipamsAssertion getTypeFromLabel(String label) {
            MipamsAssertion result = null;

            if (label == null) {
                return null;
            }

            for (MipamsAssertion type : values()) {
                if (label.equals(type.getBaseLabel()) || label.startsWith(String.format("%s__", type.getBaseLabel()))) {
                    result = type;
                    break;
                }
            }

            return result;
        }

        public String getBaseLabel() {
            return baseLabel;
        }

        public boolean isRedactableOrThrowException() throws ValidationException {
            if (!isRedactable) {
                switch (this) {
                    case ACTION:
                    case ACTION_V1:
                        throw new ValidationException(ValidationCode.ASSERTION_ACTION_REDACTED);
                    case CONTENT_BINDING:
                        throw new ValidationException(ValidationCode.ASSERTION_DATA_HASH_REDACTED);
                    default:
                        throw new ValidationException(ValidationCode.GENERAL_ERROR);
                }
            }

            return isRedactable;
        }
    }

    @Autowired
    CborContentType cborContentType;

    @Autowired
    JsonContentType jsonContentType;

    @Autowired
    EmbeddedFileContentType embeddedFileContentType;

    @Autowired
    CoreGeneratorService coreGeneratorService;

    public String getBaseLabel(Assertion assertion) throws MipamsException {
        MipamsAssertion type = MipamsAssertion.getTypeFromLabel(assertion.getLabel());
        return type.getBaseLabel();
    }

    public boolean labelReferencesContentBindingAssertion(String label) {
        return MipamsAssertion.CONTENT_BINDING.getBaseLabel().equals(label);
    }

    public MipamsAssertion getAssertionTypeFromJumbfBox(JumbfBox assertionJumbfBox) throws MipamsException {
        String label = assertionJumbfBox.getDescriptionBox().getLabel();
        return MipamsAssertion.getTypeFromLabel(label);
    }

    public boolean isJumbfBoxAnAssertion(JumbfBox assertionJumbfBox) throws MipamsException {
        return getAssertionTypeFromJumbfBox(assertionJumbfBox) != null;
    }

    public Assertion convertJumbfBoxToAssertion(JumbfBox assertionJumbfBox) throws MipamsException {

        String label = assertionJumbfBox.getDescriptionBox().getLabel();
        MipamsAssertion type = MipamsAssertion.getTypeFromLabel(label);

        if (type == null) {
            return null;
        }

        switch (type) {
            case CONTENT_BINDING:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, BindingAssertion.class);
            case CONTENT_BINDING_BMFF:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, BindingAssertionBMFF.class);
            case SOFT_BINDING:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, SoftBindingAssertion.class);
            case ACTION_V1:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, ActionsAssertionV1.class);
            case ACTION:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, ActionsAssertion.class);
            case CAWG_IDENTIY:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IdentityAssertion.class);
            case INGREDIENT:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertion.class);
            case INGREDIENT_V2:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertionV2.class);
            case INGREDIENT_V1:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertionV1.class);
            case AI_DISCLOSURE:
                return JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, AiDisclosureAssertion.class);
            case METADATA:
                return deserializeMetadataAssertion(assertionJumbfBox);

            // --- Embedded File assertions (C2PA spec §18.12 / §18.13) ---
            case EMBEDDED_DATA:
                return deserializeEmbeddedDataAssertion(assertionJumbfBox);
            case CLAIM_THUMBNAIL:
                return deserializeThumbnailAssertion(assertionJumbfBox, true);
            case INGREDIENT_THUMBNAIL:
                return deserializeThumbnailAssertion(assertionJumbfBox, false);

            default:
                return null;
        }
    }

    // -----------------------------------------------------------------------
    // Private deserializers
    // -----------------------------------------------------------------------

    private MetadataAssertion deserializeMetadataAssertion(JumbfBox assertionJumbfBox) {
        JsonBox jsonBox = (JsonBox) assertionJumbfBox.getContentBoxList().get(0);
        MetadataAssertion assertion = new MetadataAssertion();
        assertion.setPayload(jsonBox.getContent());
        return assertion;
    }

    /**
     * Deserialises a {@code c2pa.embedded-data} JumbfBox (EmbeddedFileContentType)
     * into an {@link EmbeddedDataAssertion}.
     *
     * JUMBF structure (C2PA spec §18.12.2):
     *   content[0] → EmbeddedFileDescriptionBox (bfdb)
     *   content[1] → BinaryDataBox              (bidb)
     */
    private EmbeddedDataAssertion deserializeEmbeddedDataAssertion(JumbfBox assertionJumbfBox)
            throws MipamsException {

        EmbeddedFileDescriptionBox efdb =
                (EmbeddedFileDescriptionBox) assertionJumbfBox.getContentBoxList().get(0);
        BinaryDataBox bidb =
                (BinaryDataBox) assertionJumbfBox.getContentBoxList().get(1);

        EmbeddedDataAssertion assertion = new EmbeddedDataAssertion();
        assertion.setLabel(assertionJumbfBox.getDescriptionBox().getLabel());
        assertion.setMediaType(efdb.getMediaType().toString());

        if (efdb.fileNameExists()) {
            assertion.setFileName(efdb.getFileName());
        }

        assertion.setData(readBytesFromBinaryDataBox(bidb));
        return assertion;
    }

    /**
     * Deserialises a thumbnail JumbfBox (EmbeddedFileContentType) into a
     * {@link ThumbnailAssertion}.
     *
     * JUMBF structure (C2PA spec §18.13.1.3 → identical to embedded data §18.12.2):
     *   content[0] → EmbeddedFileDescriptionBox (bfdb)
     *   content[1] → BinaryDataBox              (bidb)
     *
     * @param isClaimThumbnail {@code true}  → {@code c2pa.thumbnail.claim}
     *                         {@code false} → {@code c2pa.thumbnail.ingredient}
     */
    private ThumbnailAssertion deserializeThumbnailAssertion(JumbfBox assertionJumbfBox,
                                                              boolean isClaimThumbnail)
            throws MipamsException {

        EmbeddedFileDescriptionBox efdb =
                (EmbeddedFileDescriptionBox) assertionJumbfBox.getContentBoxList().get(0);
        BinaryDataBox bidb =
                (BinaryDataBox) assertionJumbfBox.getContentBoxList().get(1);

        ThumbnailAssertion result = new ThumbnailAssertion();

        if (isClaimThumbnail) {
            result.setIsClaimCreationTime();
        } else {
            result.setIsClaimIngredientThumbnail();
        }

        result.setMediaType(efdb.getMediaType());

        if (efdb.fileNameExists()) {
            result.setFileName(efdb.getFileName());
        }

        result.setData(readBytesFromBinaryDataBox(bidb));
        return result;
    }

    /**
     * Reads the raw bytes stored in a {@link BinaryDataBox}.
     * The box may hold either an in-memory path (written by
     * {@link org.mipams.jpegtrust.entities.assertions.EmbeddedFileContentTypeAssertion})
     * or a file URL from a parsed JUMBF stream.
     */
    private byte[] readBytesFromBinaryDataBox(BinaryDataBox bidb) throws MipamsException {
        String fileUrl = bidb.getFileUrl();
        if (fileUrl == null) {
            throw new MipamsException("BinaryDataBox has no fileUrl");
        }
        try {
            return Files.readAllBytes(java.nio.file.Path.of(fileUrl));
        } catch (IOException e) {
            throw new MipamsException("Failed to read bytes from BinaryDataBox at: " + fileUrl, e);
        }
    }

    // -----------------------------------------------------------------------
    // Ingredient helpers (unchanged)
    // -----------------------------------------------------------------------

    public Optional<HashedUriReference> extractTargetManifestFromIngredient(Assertion assertion)
            throws MipamsException {
        HashedUriReference ref = null;
        if (assertion.getClass().equals(IngredientAssertion.class)) {
            IngredientAssertion ingredientAssertionV3 = (IngredientAssertion) assertion;
            if (ingredientAssertionV3.getActiveManifestOfIngredient() != null) {
                ref = ingredientAssertionV3.getActiveManifestOfIngredient();
            }
        } else if (assertion.getClass().equals(IngredientAssertionV1.class)) {
            IngredientAssertionV1 ingredientAssertionV1 = (IngredientAssertionV1) assertion;
            if (ingredientAssertionV1.getManifestReference() != null) {
                ref = ingredientAssertionV1.getManifestReference();
            }
        } else if (assertion.getClass().equals(IngredientAssertionV2.class)) {
            IngredientAssertionV2 ingredientAssertionV2 = (IngredientAssertionV2) assertion;
            if (ingredientAssertionV2.getIngredientReference() != null) {
                ref = ingredientAssertionV2.getIngredientReference();
            }
        } else {
            throw new MipamsException(ValidationCode.GENERAL_ERROR.getCode());
        }
        return ref != null ? Optional.of(ref) : Optional.empty();
    }

    public String extractIngredientProfile(Assertion assertion) throws MipamsException {
        if (assertion.getClass().equals(IngredientAssertion.class)) {
            return ((IngredientAssertion) assertion).getRelationship();
        } else if (assertion.getClass().equals(IngredientAssertionV1.class)) {
            return ((IngredientAssertionV1) assertion).getRelationship();
        } else if (assertion.getClass().equals(IngredientAssertionV2.class)) {
            return ((IngredientAssertionV2) assertion).getRelationship();
        } else {
            throw new MipamsException(ValidationCode.GENERAL_ERROR.getCode());
        }
    }
}