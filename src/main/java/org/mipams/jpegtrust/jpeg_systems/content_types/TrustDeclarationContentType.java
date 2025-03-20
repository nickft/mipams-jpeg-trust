package org.mipams.jpegtrust.jpeg_systems.content_types;

import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.services.validation.consumer.AssertionConsumer;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrustDeclarationContentType extends TrustManifestContentType {

    @Autowired
    AssertionConsumer assertionConsumer;

    @Override
    public String getContentTypeUuid() {
        return "63326D64-0011-0010-8000-00AA00389B71";
    }

    @Override
    public void validateTypeOfAssertions(JumbfBox manifestJumbfBox) throws ValidationException {
        boolean result;

        try {
            result = assertionConsumer.containsHardBindingToContentAssertion(manifestJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (!result) {
            throw new ValidationException(ValidationCode.CLAIM_HARD_BINDINGS_MISSING);
        }

        try {
            result = assertionConsumer
                    .containsMultipleHardBindingToContentAssertions(manifestJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (result) {
            throw new ValidationException(ValidationCode.CLAIM_HARD_BINDINGS_MISSING);
        }

        try {
            result = assertionConsumer.containsActionAssertion(manifestJumbfBox);
        } catch (MipamsException e) {
            throw new ValidationException(ValidationCode.GENERAL_ERROR, e);
        }

        if (!result) {
            throw new ValidationException(ValidationCode.CLAIM_HARD_BINDINGS_MISSING);
        }

    }
}
