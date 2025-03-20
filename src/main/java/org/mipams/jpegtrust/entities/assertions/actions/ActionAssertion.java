package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.enums.ActionReason;
import org.mipams.jpegtrust.entities.assertions.region.Region;

public class ActionAssertion {
    private String action;
    private String softwareAgent;
    private Integer softwareAgentIndex;
    private String description;
    private String digitalSourceType;
    private String when;
    private List<Region> changes;
    private List<ActionAssertion> related;
    private ActionReason reason;
    private ParametersMap parameters;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSoftwareAgent() {
        return softwareAgent;
    }

    public void setSoftwareAgent(String softwareAgent) {
        this.softwareAgent = softwareAgent;
    }

    public Integer getSoftwareAgentIndex() {
        return softwareAgentIndex;
    }

    public void setSoftwareAgentIndex(Integer softwareAgentIndex) {
        this.softwareAgentIndex = softwareAgentIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDigitalSourceType() {
        return digitalSourceType;
    }

    public void setDigitalSourceType(String digitalSourceType) {
        this.digitalSourceType = digitalSourceType;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public List<Region> getChanges() {
        return changes;
    }

    public void setChanges(List<Region> changes) {
        this.changes = changes;
    }

    public List<ActionAssertion> getRelated() {
        return related;
    }

    public void setRelated(List<ActionAssertion> related) {
        this.related = related;
    }

    public ActionReason getReason() {
        return reason;
    }

    public void setReason(ActionReason reason) {
        this.reason = reason;
    }

    public ParametersMap getParameters() {
        return parameters;
    }

    public void setParameters(ParametersMap parameters) {
        this.parameters = parameters;
    }
}
