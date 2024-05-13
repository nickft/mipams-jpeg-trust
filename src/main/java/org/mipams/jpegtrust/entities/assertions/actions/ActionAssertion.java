package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;
import java.util.Map;


public class ActionAssertion {
    private String action;
    private String when;
    private String softwareAgent;
    private String changed;
    private String instanceID;
    private List<ActionAssertion> related;
    private Map<String, String> parameters;
    private String digitalSourceType;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChanged() {
        return changed;
    }

    public void setChanged(String changed) {
        this.changed = changed;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public List<ActionAssertion> getRelated() {
        return related;
    }

    public void setRelated(List<ActionAssertion> related) {
        this.related = related;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getSoftwareAgent() {
        return softwareAgent;
    }

    public void setSoftwareAgent(String softwareAgent) {
        this.softwareAgent = softwareAgent;
    }
}
