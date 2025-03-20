package org.mipams.jpegtrust.entities.assertions.ingredients;

import java.util.List;

import org.mipams.jpegtrust.entities.validation.ValidationCode;

public class StatusCodesMap {
    private List<ValidationCode> success;
    private List<ValidationCode> informational;
    private List<ValidationCode> failure;

    public List<ValidationCode> getSuccess() {
        return success;
    }

    public void setSuccess(List<ValidationCode> success) {
        this.success = success;
    }

    public List<ValidationCode> getInformational() {
        return informational;
    }

    public void setInformational(List<ValidationCode> informational) {
        this.informational = informational;
    }

    public List<ValidationCode> getFailure() {
        return failure;
    }

    public void setFailure(List<ValidationCode> failure) {
        this.failure = failure;
    }
}
