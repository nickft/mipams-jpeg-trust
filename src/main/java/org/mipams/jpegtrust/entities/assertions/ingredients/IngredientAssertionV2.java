package org.mipams.jpegtrust.entities.assertions.ingredients;

import java.util.List;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jpegtrust.entities.assertions.metadata.StatusMap;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngredientAssertionV2 extends CborAssertion {

    @Override
    public String getLabel() {
        return "c2pa.ingredient.v2";
    }

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("dc:format")
    private String mediaType;

    private String relationship;

    @JsonProperty("documentID")
    private String documentId;

    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("data")
    private HashedUriReference data;

    @JsonProperty("data_types")
    private HashedUriReference dataTypes;

    @JsonProperty("c2pa_manifest")
    private HashedUriReference ingredientReference;

    @JsonProperty("thumbnail")
    private HashedUriReference thumbnail;

    @JsonProperty("validatedStatus")
    private List<StatusMap> validationStatus;

    @JsonProperty("description")
    private String description;

    @JsonProperty("informational_URI")
    private String informationalUri;

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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    public HashedUriReference getData() {
        return data;
    }

    public void setData(HashedUriReference data) {
        this.data = data;
    }

    public HashedUriReference getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(HashedUriReference dataTypes) {
        this.dataTypes = dataTypes;
    }

    public HashedUriReference getIngredientReference() {
        return ingredientReference;
    }

    public void setIngredientReference(HashedUriReference ingredientReference) {
        this.ingredientReference = ingredientReference;
    }

    public HashedUriReference getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(HashedUriReference thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<StatusMap> getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(List<StatusMap> validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInformationalUri() {
        return informationalUri;
    }

    public void setInformationalUri(String informationalUri) {
        this.informationalUri = informationalUri;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }

}
