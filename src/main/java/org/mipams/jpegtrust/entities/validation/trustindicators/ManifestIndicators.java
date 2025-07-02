package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.HashMap;
import java.util.Map;

import org.mipams.jpegtrust.entities.assertions.Assertion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ManifestIndicators implements ManifestIndicatorsInterface {

    String label;

    Map<String, Assertion> assertions = new HashMap<>();

    ClaimIndicatorsInterface claim;

    @JsonProperty("claim_signature")
    ClaimSignatureIndicators claimSignature;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Assertion> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Assertion> assertions) {
        this.assertions = assertions;
    }

    public ClaimIndicatorsInterface getClaim() {
        return claim;
    }

    public void setClaim(ClaimIndicatorsInterface claim) {
        this.claim = claim;
    }

    public ClaimSignatureIndicators getClaimSignature() {
        return claimSignature;
    }

    public void setClaimSignature(ClaimSignatureIndicators claimSignature) {
        this.claimSignature = claimSignature;
    }
}
