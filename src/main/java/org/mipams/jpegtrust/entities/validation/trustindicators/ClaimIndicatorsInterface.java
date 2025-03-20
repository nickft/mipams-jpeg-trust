package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.Map;
import org.mipams.jpegtrust.entities.validation.ValidationCode;

public interface ClaimIndicatorsInterface extends EntityIndicators {

    void setContentStatus(ValidationCode statusCode);

    void setSignatureStatus(ValidationCode claimSignatureValidated);

    void setSignature(String claimSignatureUri);

    Map<String, String> getAssertionStatus();

}
