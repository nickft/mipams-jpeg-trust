package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ActionsAssertion extends CborAssertion {
    private List<ActionAssertion> actions;
    private List<ActionTemplate> templates;
    private List<GeneratorInfoMap> softwareAgents;
    private AssertionMetadata metadata;
    private Boolean allActionsIncluded;

    @Override
    @JsonIgnore
    public boolean isReductable() throws MipamsException {
        throw new ValidationException(ValidationCode.ASSERTION_ACTION_REDACTED);
    }

    @Override
    @JsonIgnore
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

    public List<GeneratorInfoMap> getSoftwareAgents() {
        return softwareAgents;
    }

    public void setSoftwareAgents(List<GeneratorInfoMap> softwareAgents) {
        this.softwareAgents = softwareAgents;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }

    public Boolean getAllActionsIncluded() {
        return allActionsIncluded;
    }

    public void setAllActionsIncluded(boolean val) {
        this.allActionsIncluded = val;
    }
}