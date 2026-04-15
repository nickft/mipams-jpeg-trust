package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mipams.jpegtrust.entities.HashedUriReference;
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
    private Map<String, Object> parameters;

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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    private void ensureParametersInitialized() {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
    }

    public String getRedacted() {
        if (parameters == null || parameters.get("redacted") == null) {
            return null;
        }
        return String.valueOf(parameters.get("redacted"));
    }

    public void setRedacted(String redacted) {
        ensureParametersInitialized();
        parameters.put("redacted", redacted);
    }

    @SuppressWarnings("unchecked")
    public List<HashedUriReference> getIngredients() {
        if (parameters == null) {
            return null;
        }
        return (List<HashedUriReference>) parameters.get("ingredients");
    }

    public void setIngredients(List<HashedUriReference> ingredients) {
        ensureParametersInitialized();
        parameters.put("ingredients", ingredients);
    }

    public String getSourceLanguage() {
        if (parameters == null || parameters.get("sourceLanguage") == null) {
            return null;
        }
        return String.valueOf(parameters.get("sourceLanguage"));
    }

    public void setSourceLanguage(String sourceLanguage) {
        ensureParametersInitialized();
        parameters.put("sourceLanguage", sourceLanguage);
    }

    public String getTargetLanguage() {
        if (parameters == null || parameters.get("targetLanguage") == null) {
            return null;
        }
        return String.valueOf(parameters.get("targetLanguage"));
    }

    public void setTargetLanguage(String targetLanguage) {
        ensureParametersInitialized();
        parameters.put("targetLanguage", targetLanguage);
    }
}
