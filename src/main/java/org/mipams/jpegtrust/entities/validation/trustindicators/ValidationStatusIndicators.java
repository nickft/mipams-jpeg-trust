package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.HashMap;
import java.util.Map;
import org.mipams.jpegtrust.entities.validation.ValidationCode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationStatusIndicators implements EntityIndicators {

    @JsonProperty("signature_status")
    private String signatureStatus = "";

    @JsonProperty("assertion_status")
    private Map<String, String> assertionStatus = new HashMap<>();

    @JsonProperty("content_status")
    private String contentStatus = "";

    @JsonProperty("trust_status")
    private String trustStatus = "";

    public String getSignatureStatus() {
        return signatureStatus;
    }

    public void setSignatureStatus(ValidationCode signatureStatus) {
        this.signatureStatus = signatureStatus.getCode();
    }

    public Map<String, String> getAssertionStatus() {
        return assertionStatus;
    }

    public void setAssertionStatus(Map<String, String> assertionStatus) {
        this.assertionStatus = assertionStatus;
    }

    public String getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(ValidationCode contentStatus) {
        this.contentStatus = contentStatus.getCode();
    }

}