package org.mipams.jpegtrust.jpeg_systems.content_types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertion;
import org.mipams.jpegtrust.entities.assertions.actions.ActionAssertionV1;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertion;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV1;
import org.mipams.jpegtrust.entities.assertions.ingredients.IngredientAssertionV2;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.services.validation.consumer.AssertionConsumer;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.JumbfUriUtils;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateManifestContentType extends TrustManifestContentType {

    @Autowired
    AssertionConsumer assertionConsumer;

    @Override
    public String getContentTypeUuid() {
        return "6332756D-0011-0010-8000-00AA00389B71";
    }

    @Override
    public void validateTypeOfAssertions(JumbfBox manifestJumbfBox) throws ValidationException {

        boolean result;

        try {
            result = assertionConsumer.containsMultipleIngredientAssertions(manifestJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (result) {
            throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
        }

        Optional<Assertion> optionalAssertion = assertionConsumer.getIngredientAssertion(manifestJumbfBox);
        Assertion assertion;

        if (optionalAssertion.isEmpty()) {
            throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
        }
        assertion = optionalAssertion.get();

        try {
            if (!assertionConsumer.containsIngredientsOfRelationshipParentOf(manifestJumbfBox)) {
                throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
            }
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (assertion.getClass().equals(IngredientAssertion.class)) {
            IngredientAssertion ingr = (IngredientAssertion) assertion;
            if (!JumbfUriUtils.isJumbfUriReferenceValid(ingr.getActiveManifestOfIngredient().getUrl())
                    || !JumbfUriUtils.isJumbfUriReferenceValid(ingr.getClaimSignatureOfIngredient().getUrl())) {
                throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
            }

        } else if (assertion.getClass().equals(IngredientAssertionV2.class)) {
            IngredientAssertionV2 ingr = (IngredientAssertionV2) assertion;
            if (!JumbfUriUtils.isJumbfUriReferenceValid(ingr.getIngredientReference().getUrl())) {
                throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
            }
        } else if (assertion.getClass().equals(IngredientAssertionV1.class)) {
            IngredientAssertionV1 ingr = (IngredientAssertionV1) assertion;
            if (!JumbfUriUtils.isJumbfUriReferenceValid(ingr.getManifestReference().getUrl())) {
                throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
            }
        } else {
            throw new ValidationException(ValidationCode.MANIFEST_UPDATE_WRONG_PARENTS);
        }

        try {
            result = assertionConsumer.containsHardBindingToContentAssertion(manifestJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (result) {
            throw new ValidationException(ValidationCode.MANIFEST_UPDATE_INVALID);
        }

        try {
            result = assertionConsumer.containsActionAssertion(manifestJumbfBox);
        } catch (MipamsException e) {
        }

        if (result) {

            List<Assertion> assertions;
            try {
                assertions = assertionConsumer.getActionAssertions(manifestJumbfBox);
            } catch (MipamsException e) {
                throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
            }

            List<String> allowedActionChoices = getListOfAllowedActions();

            for (Assertion assertionIterator : assertions) {
                if (assertionIterator.getClass().equals(ActionAssertion.class)) {
                    ActionAssertion actionAssertion = (ActionAssertion) assertionIterator;
                    if (!allowedActionChoices.contains(actionAssertion.getAction())) {
                        throw new ValidationException(ValidationCode.MANIFEST_UPDATE_INVALID);
                    }
                } else if (assertionIterator.getClass().equals(ActionAssertionV1.class)) {
                    ActionAssertionV1 actionAssertion = (ActionAssertionV1) assertionIterator;
                    if (!allowedActionChoices.contains(actionAssertion.getAction())) {
                        throw new ValidationException(ValidationCode.MANIFEST_UPDATE_INVALID);
                    }
                } else {
                    throw new ValidationException(ValidationCode.MANIFEST_UPDATE_INVALID);
                }
            }

        }
    }

    private List<String> getListOfAllowedActions() {
        return new ArrayList<>(List.of(
                ActionChoice.C2PA_EDITED_METADATA.getValue(),
                ActionChoice.C2PA_OPENED.getValue(),
                ActionChoice.C2PA_PUBLISHED.getValue(),
                ActionChoice.C2PA_REDACTED.getValue()));
    }
}