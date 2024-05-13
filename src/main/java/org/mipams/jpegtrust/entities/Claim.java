package org.mipams.jpegtrust.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "dc:title", "dc:format", "instanceID", "claim_generator", "claim_generator_info", "signature", "assertions" })
public class Claim implements ProvenanceEntity {

    @JsonProperty("claim_generator")
    private String claimGenerator;

    @JsonProperty("claim_generator_info")
    private Map<String, String> claimGeneratorInfo = new HashMap<>();

    private String signature;

    private TreeSet<HashedUriReference> assertions = new TreeSet<>();

    @JsonProperty("dc:format")
    private String mediaType;

    @JsonProperty("dc:title")
    private String title;
    
    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("redacted_assertions")
    private List<String> redactedAssertions;

    @JsonProperty("alg")
    private String algorithm;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public TreeSet<HashedUriReference> getAssertions() {
        return assertions;
    }

    public void setAssertions(TreeSet<HashedUriReference> assertions) {
        this.assertions = assertions;
    }

    public Map<String, String> getClaimGeneratorInfo() {
        return claimGeneratorInfo;
    }

    public void setClaimGeneratorInfo(Map<String, String> claimGeneratorInfo) {
        this.claimGeneratorInfo = claimGeneratorInfo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getRedactedAssertions() {
        return redactedAssertions;
    }
 
    public void setRedactedAssertions(List<String> redactedAssertions) {
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
}
