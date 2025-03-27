package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaimSignatureIndicators {

    @JsonProperty("signature_algorithm")
    private String signatureAlgorithm;

    @JsonProperty("subject")
    private Map<String, String> subject = new HashMap<>();

    @JsonProperty("issuer")
    private Map<String, String> issuer = new HashMap<>();

    @JsonProperty("validity")
    private Map<String, String> validity = new HashMap<>();

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public Map<String, String> getSubject() {
        return subject;
    }

    public void setSubject(Map<String, String> subject) {
        this.subject = subject;
    }

    public Map<String, String> getIssuer() {
        return issuer;
    }

    public void setIssuer(Map<String, String> issuer) {
        this.issuer = issuer;
    }

    public Map<String, String> getValidity() {
        return validity;
    }

    public void setValidity(Map<String, String> validity) {
        this.validity = validity;
    }
}
