package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.HashMap;
import java.util.Map;

import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.validation.ValidationCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaimIndicators extends Claim implements ClaimIndicatorsInterface {

    @JsonProperty("signature_status")
    private String signatureStatus;

    @JsonProperty("assertion_status")
    private Map<String, String> assertionStatus = new HashMap<>();

    @JsonProperty("content_status")
    private String contentStatus;

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

    public ClaimIndicators() {
    }

    public ClaimIndicators(Claim claim) {
        setInstanceId(claim.getInstanceId());
        setClaimGeneratorInfo(claim.getClaimGeneratorInfo());
        setSignature(claim.getSignature());
        setCreatedAssertions(claim.getCreatedAssertions());
        setGatheredAssertions(claim.getGatheredAssertions());
        setTitle(claim.getTitle());
        setRedactedAssertions(claim.getRedactedAssertions());
        setAlgorithm(claim.getAlgorithm());
        setAlgorithmSoftware(claim.getAlgorithmSoftware());
    }

    @JsonIgnore
    @Override
    public String getClaimIndicatorKeyName() {
        return "claim.v2";
    }
}
