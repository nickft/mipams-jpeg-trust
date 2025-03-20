package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.util.MipamsException;

public class ActionsAssertionV1 extends CborAssertion {
    private List<ActionAssertionV1> actions;
    private AssertionMetadata metadata;

    @Override
    public String getLabel() {
        return "c2pa.actions";
    }

    @Override
    public boolean isReductable() throws MipamsException {
        throw new ValidationException(ValidationCode.ASSERTION_ACTION_REDACTED);
    }

    public List<ActionAssertionV1> getActions() {
        return actions;
    }

    public void setActions(List<ActionAssertionV1> actions) {
        this.actions = actions;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }
}
