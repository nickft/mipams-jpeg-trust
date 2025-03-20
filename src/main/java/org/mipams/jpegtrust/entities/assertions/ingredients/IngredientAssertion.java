package org.mipams.jpegtrust.entities.assertions.ingredients;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngredientAssertion extends CborAssertion {

    @Override
    public String getLabel() {
        return "c2pa.ingredient.v3";
    }

    @JsonProperty("dc:title")
    private String title;

    @JsonProperty("dc:format")
    private String mediaType;

    @JsonProperty("relationship")
    private String relationship;

    @JsonProperty("validationResultsMap")
    private ValidationResultsMap validationResults;

    @JsonProperty("instanceID")
    private String instanceId;

    @JsonProperty("data")
    private HashedUriReference data;

    @JsonProperty("dataTypes")
    private HashedUriReference dataTypes;

    @JsonProperty("activeManifest")
    private HashedUriReference activeManifestOfIngredient;

    @JsonProperty("claimSignature")
    private HashedUriReference claimSignatureOfIngredient;

    @JsonProperty("thumbnail")
    private HashedUriReference thumbnail;

    @JsonProperty("description")
    private String description;

    @JsonProperty("informationalURI")
    private String informationalUri;

    @JsonProperty("metadata")
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

    public ValidationResultsMap getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(ValidationResultsMap validationResults) {
        this.validationResults = validationResults;
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

    public HashedUriReference getActiveManifestOfIngredient() {
        return activeManifestOfIngredient;
    }

    public void setActiveManifestOfIngredient(HashedUriReference activeManifestOfIngredient) {
        this.activeManifestOfIngredient = activeManifestOfIngredient;
    }

    public HashedUriReference getClaimSignatureOfIngredient() {
        return claimSignatureOfIngredient;
    }

    public void setClaimSignatureOfIngredient(HashedUriReference claimSignatureOfIngredient) {
        this.claimSignatureOfIngredient = claimSignatureOfIngredient;
    }

    public HashedUriReference getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(HashedUriReference thumbnail) {
        this.thumbnail = thumbnail;
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
