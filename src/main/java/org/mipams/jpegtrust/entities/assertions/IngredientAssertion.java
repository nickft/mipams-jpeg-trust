package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngredientAssertion extends CborAssertion {

    public final static String RELATIONSHIP_PARENT_OF = "parentOf";
    public final static String RELATIONSHIP_COMPONENT_OF = "componentOf";
    public final static String RELATIONSHIP_INPUT_OF = "inputTo";

    @Override
    public String getLabel() {
        return "c2pa.ingredient";
    }

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("dc:format")
    private String mediaType;

    private String thumbnailURL;
    
    @JsonProperty("instanceID")
    private String instanceId;
    
    private String relationship;

    @JsonProperty("c2pa_manifest")
    private HashedUriReference ingredientReference;

    private AssertionMetadata metadata;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public HashedUriReference getManifestReference() {
        return ingredientReference;
    }

    public void setManifestReference(HashedUriReference manifestReference) {
        this.ingredientReference = manifestReference;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }

}
