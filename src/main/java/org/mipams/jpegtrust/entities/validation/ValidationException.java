package org.mipams.jpegtrust.entities.validation;

import org.mipams.jumbf.util.MipamsException;

public class ValidationException extends MipamsException {

    private ValidationCode statusCode = ValidationCode.GENERAL_ERROR;

    public ValidationCode getStatusCode() {
        return statusCode;
    }

    public ValidationException() {
        super();
    }

    public ValidationException(ValidationCode statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public ValidationException(ValidationCode statusCode, Throwable e) {
        super(e);
        this.statusCode = statusCode;
    }
}
