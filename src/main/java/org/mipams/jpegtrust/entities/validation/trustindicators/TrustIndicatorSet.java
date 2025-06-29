package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TrustIndicatorSet {
    @JsonProperty("@context")
    List<String> contexts = new ArrayList<>(List.of("http://jpeg.org/jpegtrust/"));

    @JsonProperty("content")
    MediaContentIndicators content;

    @JsonProperty("declaration")
    ManifestIndicatorsInterface declaration;

    @JsonProperty("manifests")
    List<ManifestIndicatorsInterface> manifests = new ArrayList<>();

    public List<String> getContexts() {
        return contexts;
    }

    public void setContexts(List<String> contexts) {
        this.contexts = contexts;
    }

    public MediaContentIndicators getContent() {
        return content;
    }

    public void setContent(MediaContentIndicators content) {
        this.content = content;
    }

    public ManifestIndicatorsInterface getDeclaration() {
        return declaration;
    }

    public void setDeclaration(ManifestIndicatorsInterface declaration) {
        this.declaration = declaration;
    }

    public List<ManifestIndicatorsInterface> getManifests() {
        return manifests;
    }

    public void setManifests(List<ManifestIndicatorsInterface> manifests) {
        this.manifests = manifests;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return this.toString();
        }
    }
}
