package org.mipams.jpegtrust.entities.assertions.ingredients;

import java.util.List;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jpegtrust.entities.assertions.metadata.StatusMap;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngredientAssertionV1 extends CborAssertion {

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

    @JsonProperty("documentID")
    private String documentId;

    @JsonProperty("instanceID")
    private String instanceId;

    private String relationship;

    @JsonProperty("c2pa_manifest")
    private HashedUriReference ingredientReference;

    private HashedUriReference thumbnail;

    @JsonProperty("validationStatus")
    private List<StatusMap> validationStatus;

    private AssertionMetadata metadata;

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public HashedUriReference getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(HashedUriReference thumbnail) {
        this.thumbnail = thumbnail;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }

}
