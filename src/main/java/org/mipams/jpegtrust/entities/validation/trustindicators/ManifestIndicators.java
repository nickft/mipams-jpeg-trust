package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.HashMap;
import java.util.Map;

import org.mipams.jpegtrust.entities.assertions.Assertion;

public class ManifestIndicators implements ManifestIndicatorsInterface {

    Map<String, Assertion> assertions = new HashMap<>();

    ClaimIndicatorsInterface claim;

    ClaimSignatureIndicators signature;

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

    public ClaimSignatureIndicators getSignature() {
        return signature;
    }

    public void setSignature(ClaimSignatureIndicators signature) {
        this.signature = signature;
    }
}
