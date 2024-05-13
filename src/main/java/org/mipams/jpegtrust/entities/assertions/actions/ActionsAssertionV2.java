package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;

public class ActionsAssertionV2 extends CborAssertion {
    private List<ActionAssertion> actions;
    private List<ActionTemplate> templates;
    private ActionMetadata metadata;

    @Override
    public String getLabel() {
        return "c2pa.actions.v2";
    }

    public List<ActionAssertion> getActions() {
        return actions;
    }

    public void setActions(List<ActionAssertion> actions) {
        this.actions = actions;
    }

    public List<ActionTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ActionTemplate> templates) {
        this.templates = templates;
    }

    public ActionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ActionMetadata metadata) {
        this.metadata = metadata;
    }
}