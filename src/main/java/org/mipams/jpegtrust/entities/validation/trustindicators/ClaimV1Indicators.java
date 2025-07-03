package org.mipams.jpegtrust.entities.validation.trustindicators;

import org.mipams.jpegtrust.entities.ClaimV1;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClaimV1Indicators extends ClaimV1 implements ClaimIndicatorsInterface {

    public ClaimV1Indicators() {
    }

    public ClaimV1Indicators(ClaimV1 claim) {
        setInstanceId(claim.getInstanceId());
        setClaimGeneratorInfo(claim.getClaimGeneratorInfo());
        setSignature(claim.getSignature());
        setAssertions(claim.getAssertions());
        setTitle(claim.getTitle());
        setRedactedAssertions(claim.getRedactedAssertions());
        setAlgorithm(claim.getAlgorithm());
        setMediaType(claim.getMediaType());
        setMetadata(claim.getMetadata());
    }

    @JsonIgnore
    @Override
    public String getClaimIndicatorKeyName() {
        return "claim";
    }
}
