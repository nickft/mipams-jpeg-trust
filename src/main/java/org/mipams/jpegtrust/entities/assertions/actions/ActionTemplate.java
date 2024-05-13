package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.Map;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.enums.ActionChoice;

public class ActionTemplate {
    private ActionChoice action;
    private String softwareAgent;
    private String description;
    private String digitalSourceType;
    private HashedUriReference icon;
    private Map<String, String> templateParameters;

    public ActionChoice getAction() {
        return action;
    }

    public void setAction(ActionChoice action) {
        this.action = action;
    }

    public String getSoftwareAgent() {
        return softwareAgent;
    }

    public void setSoftwareAgent(String softwareAgent) {
        this.softwareAgent = softwareAgent;
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

    public HashedUriReference getIcon() {
        return icon;
    }

    public void setIcon(HashedUriReference icon) {
        this.icon = icon;
    }

    public Map<String, String> getTemplateParameters() {
        return templateParameters;
    }

    public void setTemplateParameters(Map<String, String> templateParameters) {
        this.templateParameters = templateParameters;
    }
}

