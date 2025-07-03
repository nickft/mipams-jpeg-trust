package org.mipams.jpegtrust.entities.validation.trustindicators;

public interface ClaimIndicatorsInterface extends EntityIndicators {
    String getClaimIndicatorKeyName();

    void setSignature(String claimSignatureUri);
}
