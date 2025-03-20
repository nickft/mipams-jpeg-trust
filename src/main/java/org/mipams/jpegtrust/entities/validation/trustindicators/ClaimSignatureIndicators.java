package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaimSignatureIndicators {

    @JsonProperty("signature_algorithm")
    private String signatureAlgorithm;

    @JsonProperty("subject")
    private Map<String, String> subject;

    @JsonProperty("issuer")
    private Map<String, String> issuer;

    @JsonProperty("validity")
    private Map<String, String> validity;
}
