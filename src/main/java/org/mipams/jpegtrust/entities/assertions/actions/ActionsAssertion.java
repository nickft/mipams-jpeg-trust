package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;
import java.util.Map;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.util.MipamsException;

public class ActionsAssertion extends CborAssertion {
    private List<ActionAssertion> actions;
    private List<ActionTemplate> templates;
    private Map<String, String> softwareAgents;
    private AssertionMetadata metadata;

    @Override
    public boolean isReductable() throws MipamsException {
        throw new ValidationException(ValidationCode.ASSERTION_ACTION_REDACTED);
    }

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

    public Map<String, String> getSoftwareAgents() {
        return softwareAgents;
    }

    public void setSoftwareAgents(Map<String, String> softwareAgents) {
        this.softwareAgents = softwareAgents;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }
}
