package org.mipams.jpegtrust.entities;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "claim_generator", "claim_generator_info", "signature", "assertions", "dc:format", "instanceID",
        "dc:title", "redacted_assertions", "alg", "alg_soft", "metadata" })
public class ClaimV1 implements ProvenanceEntity {

    @JsonProperty("claim_generator")
    private String claimGenerator;

    @JsonProperty("claim_generator_info")
    private LinkedHashSet<LinkedHashMap<String, String>> claimGeneratorInfo = new LinkedHashSet<>();

    private String signature;

    private LinkedHashSet<HashedUriReference> assertions = new LinkedHashSet<>();

    @JsonProperty("dc:format")
    private String mediaType;

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("redacted_assertions")
    private LinkedHashSet<String> redactedAssertions = new LinkedHashSet<>();

    @JsonProperty("alg")
    private String algorithm;

    @JsonProperty("alg_soft")
    private String algorithmSoftware;

    @JsonProperty("metadata")
    private AssertionMetadata metadata;

    @JsonIgnore
    public boolean isMalformed() {
        return instanceId == null || signature == null || assertions.isEmpty()
                || claimGeneratorInfo == null;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public LinkedHashSet<HashedUriReference> getAssertions() {
        return assertions;
    }

    public void setAssertions(LinkedHashSet<HashedUriReference> assertions) {
        this.assertions = assertions;
    }

    public LinkedHashSet<LinkedHashMap<String, String>> getClaimGeneratorInfo() {
        return claimGeneratorInfo;
    }

    public void setClaimGeneratorInfo(LinkedHashSet<LinkedHashMap<String, String>> claimGeneratorInfo) {
        this.claimGeneratorInfo = claimGeneratorInfo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public LinkedHashSet<String> getRedactedAssertions() {
        return redactedAssertions;
    }

    public void setRedactedAssertions(LinkedHashSet<String> redactedAssertions) {
        this.redactedAssertions = redactedAssertions;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClaimGenerator() {
        return claimGenerator;
    }

    public void setClaimGenerator(String generatorInfoName) {
        this.claimGenerator = generatorInfoName;
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

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }
}
