package org.mipams.jpegtrust.services.validation.discovery;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.BindingAssertion;
import org.mipams.jpegtrust.entities.assertions.SoftBindingAssertion;
import org.mipams.jpegtrust.entities.assertions.ThumbnailAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionsAssertionV1;
import org.mipams.jpegtrust.entities.assertions.cawg.IdentityAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV2;
import org.mipams.jpegtrust.entities.assertions.metadata.MetadataAssertion;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.entities.EmbeddedFileDescriptionBox;
import org.mipams.jumbf.entities.JsonBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.content_types.CborContentType;
import org.mipams.jumbf.services.content_types.EmbeddedFileContentType;
import org.mipams.jumbf.services.content_types.JsonContentType;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssertionDiscovery {

    public enum MipamsAssertion {

        CONTENT_BINDING("c2pa.hash.data"),
        SOFT_BINDING("c2pa.soft-binding", true),
        ACTION("c2pa.actions.v2"),
        ACTION_V1("c2pa.actions"),
        CAWG_IDENTIY("cawg.identity", true),
        INGREDIENT("c2pa.ingredient.v3", true),
        INGREDIENT_V2("c2pa.ingredient.v2", true),
        INGREDIENT_V1("c2pa.ingredient", true),
        METADATA("c2pa.metadata", true),
        CLAIM_THUMBNAIL("c2pa.thumbnail.claim", true),
        INGREDIENT_THUMBNAIL("c2pa.thumbnail.ingredient", true);

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

                if (label.startsWith(type.getBaseLabel())) {
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
        MipamsAssertion type = MipamsAssertion.getTypeFromLabel(label);

        return type;
    }

    public boolean isJumbfBoxAnAssertion(JumbfBox assertionJumbfBox) throws MipamsException {
        return getAssertionTypeFromJumbfBox(assertionJumbfBox) != null;
    }

    public Assertion convertJumbfBoxToAssertion(JumbfBox assertionJumbfBox) throws MipamsException {

        Assertion result;

        String label = assertionJumbfBox.getDescriptionBox().getLabel();

        MipamsAssertion type = MipamsAssertion.getTypeFromLabel(label);

        if (type == null) {
            return null;
        }

        switch (type) {
            case CONTENT_BINDING:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, BindingAssertion.class);
                break;
            case SOFT_BINDING:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, SoftBindingAssertion.class);
                break;
            case ACTION_V1:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, ActionsAssertionV1.class);
                break;
            case ACTION:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, ActionsAssertion.class);
                break;
            case CAWG_IDENTIY:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IdentityAssertion.class);
                break;
            case INGREDIENT:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertion.class);
                break;
            case INGREDIENT_V2:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertionV2.class);
                break;
            case INGREDIENT_V1:
                result = JpegTrustUtils.deserializeCborJumbfBox(assertionJumbfBox, IngredientAssertionV1.class);
                break;
            case METADATA:
                result = deserializeMetadataAssertion(assertionJumbfBox);
                break;
            case CLAIM_THUMBNAIL:
            case INGREDIENT_THUMBNAIL:
                result = deserializeThumbnailAssertion(assertionJumbfBox);
                break;
            default:
                result = null;
                break;
        }

        return result;

    }

    private MetadataAssertion deserializeMetadataAssertion(JumbfBox assertionJumbfBox) {

        JsonBox jsonBox = (JsonBox) assertionJumbfBox.getContentBoxList().get(0);

        MetadataAssertion assertion = new MetadataAssertion();
        assertion.setPayload(jsonBox.getContent());

        return assertion;
    }

    private ThumbnailAssertion deserializeThumbnailAssertion(JumbfBox assertionJumbfBox) {
        EmbeddedFileDescriptionBox edBox = (EmbeddedFileDescriptionBox) assertionJumbfBox.getContentBoxList().get(0);

        ThumbnailAssertion result = new ThumbnailAssertion();
        result.setThumbnailUrl(edBox.getFileName());
        result.setMediaType(edBox.getMediaType());

        return result;
    }

    public HashedUriReference extractTargetManifestFromIngredient(Assertion assertion) throws MipamsException {
        HashedUriReference ref;
        if (assertion.getClass().equals(IngredientAssertion.class)) {
            ref = ((IngredientAssertion) assertion).getActiveManifestOfIngredient();
        } else if (assertion.getClass().equals(IngredientAssertionV1.class)) {
            ref = ((IngredientAssertionV1) assertion).getManifestReference();
        } else if (assertion.getClass().equals(IngredientAssertionV2.class)) {
            ref = ((IngredientAssertionV2) assertion).getIngredientReference();
        } else {
            throw new MipamsException(ValidationCode.GENERAL_ERROR.getCode());
        }
        return ref;
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