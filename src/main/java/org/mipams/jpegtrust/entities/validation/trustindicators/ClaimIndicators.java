package org.mipams.jpegtrust.entities.validation.trustindicators;

import org.mipams.jpegtrust.entities.Claim;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClaimIndicators extends Claim implements ClaimIndicatorsInterface {

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
