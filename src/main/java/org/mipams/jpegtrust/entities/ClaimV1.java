package org.mipams.jpegtrust.entities;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "dc:title", "dc:format", "instanceID", "claim_generator", "claim_generator_info", "signature",
        "assertions" })
public class ClaimV1 implements ProvenanceEntity {

    @JsonProperty("claim_generator")
    private String claimGenerator;

    @JsonProperty("claim_generator_info")
    private LinkedHashMap<String, String> claimGeneratorInfo = new LinkedHashMap<>();

    private String signature;

    private LinkedHashSet<HashedUriReference> assertions = new LinkedHashSet<>();

    @JsonProperty("dc:format")
    private String mediaType;

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("redacted_assertions")
    private LinkedHashSet<String> redactedAssertions;

    @JsonProperty("alg")
    private String algorithm;

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

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }
}
