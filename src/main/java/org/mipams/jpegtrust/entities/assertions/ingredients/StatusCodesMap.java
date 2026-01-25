package org.mipams.jpegtrust.entities.assertions.ingredients;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class StatusCodesMap {
    private List<StatusMap> success = new ArrayList<>();
    private List<StatusMap> informational = new ArrayList<>();
    private List<StatusMap> failure = new ArrayList<>();

    public List<StatusMap> getSuccess() {
        return success;
    }

    public void setSuccess(List<StatusMap> success) {
        this.success = success;
    }

    public List<StatusMap> getInformational() {
        return informational;
    }

    public void setInformational(List<StatusMap> informational) {
        this.informational = informational;
    }

    public List<StatusMap> getFailure() {
        return failure;
    }

    public void setFailure(List<StatusMap> failure) {
        this.failure = failure;
    }
}
