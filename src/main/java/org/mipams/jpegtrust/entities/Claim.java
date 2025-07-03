package org.mipams.jpegtrust.entities;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "instanceID", "claim_generator_info", "signature", "created_assertions",
        "gathered_assertions", "dc:title", "redacted_assertions", "alg", "alg_soft" })
public class Claim implements ProvenanceEntity {

    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("claim_generator_info")
    private LinkedHashMap<String, String> claimGeneratorInfo = new LinkedHashMap<>();

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("created_assertions")
    private LinkedHashSet<HashedUriReference> createdAssertions = new LinkedHashSet<>();

    @JsonProperty("gathered_assertions")
    private LinkedHashSet<HashedUriReference> gatheredAssertions = new LinkedHashSet<>();

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("redacted_assertions")
    private LinkedHashSet<String> redactedAssertions = new LinkedHashSet<>();

    @JsonProperty("alg")
    private String algorithm;

    @JsonProperty("alg_soft")
    private String algorithmSoftware;

    @JsonIgnore
    public boolean isMalformed() {
        return instanceId == null || signature == null || createdAssertions.isEmpty()
                || claimGeneratorInfo == null;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public LinkedHashMap<String, String> getClaimGeneratorInfo() {
        return claimGeneratorInfo;
    }

    public void setClaimGeneratorInfo(LinkedHashMap<String, String> claimGeneratorInfo) {
        this.claimGeneratorInfo = claimGeneratorInfo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public LinkedHashSet<HashedUriReference> getCreatedAssertions() {
        return createdAssertions;
    }

    public void setCreatedAssertions(LinkedHashSet<HashedUriReference> assertions) {
        this.createdAssertions = assertions;
    }

    public LinkedHashSet<HashedUriReference> getGatheredAssertions() {
        return gatheredAssertions;
    }

    public void setGatheredAssertions(LinkedHashSet<HashedUriReference> gatheredAssertions) {
        this.gatheredAssertions = gatheredAssertions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LinkedHashSet<String> getRedactedAssertions() {
        return redactedAssertions;
    }

    public void setRedactedAssertions(LinkedHashSet<String> redactedAssertions) {
        this.redactedAssertions = redactedAssertions;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String alg) {
        this.algorithm = alg;
    }

    public String getAlgorithmSoftware() {
        return algorithmSoftware;
    }

    public void setAlgorithmSoftware(String algorithmSoftware) {
        this.algorithmSoftware = algorithmSoftware;
    }
}
