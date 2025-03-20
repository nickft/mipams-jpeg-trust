package org.mipams.jpegtrust.entities.assertions.metadata;

import org.mipams.jpegtrust.entities.validation.ValidationCode;

public class StatusMap {
    ValidationCode code;
    String url;
    String explanation;
    Boolean success;

    public ValidationCode getCode() {
        return code;
    }

    public void setCode(ValidationCode code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

}
