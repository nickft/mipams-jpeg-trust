package org.mipams.jpegtrust.entities.assertions.actions;

import java.util.List;
import java.util.Map;

import org.mipams.jpegtrust.entities.HashedUriReference;

public class ParametersMap {
    private String redacted;
    private List<HashedUriReference> ingredients;
    private String sourceLanguage;
    private String targetLanguage;
    private Map<String, String> parameters;

    public String getRedacted() {
        return redacted;
    }

    public void setRedacted(String redacted) {
        this.redacted = redacted;
    }

    public List<HashedUriReference> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<HashedUriReference> ingredients) {
        this.ingredients = ingredients;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
