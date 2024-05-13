package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;

public class ActionsAssertion extends CborAssertion {
    private List<ActionAssertion> actions;
    private ActionMetadata metadata;


    @Override
    public String getLabel() {
        return "c2pa.actions";
    }

    public List<ActionAssertion> getActions() {
        return actions;
    }

    public void setActions(List<ActionAssertion> actions) {
        this.actions = actions;
    }

    public ActionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ActionMetadata metadata) {
        this.metadata = metadata;
    }
}