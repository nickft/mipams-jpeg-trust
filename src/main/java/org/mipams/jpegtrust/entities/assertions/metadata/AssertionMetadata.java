package org.mipams.jpegtrust.entities.assertions.metadata;

import java.util.List;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jpegtrust.entities.assertions.region.Region;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssertionMetadata extends CborAssertion {

    @Override
    public String getLabel() {
        return "c2pa.assertion.metadata";
    }

    @JsonProperty("dateTime")
    private String dateTime;

    @JsonProperty("reviewRatings")
    private List<Rating> reviewRatings;

    @JsonProperty("reference")
    private HashedUriReference reference;

    private Source dataSource;

    private List<String> localizations;

    private Region regionOfInterest;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public List<Rating> getReviewRatings() {
        return reviewRatings;
    }

    public void setReviewRatings(List<Rating> reviewRatings) {
        this.reviewRatings = reviewRatings;
    }

    public HashedUriReference getReference() {
        return reference;
    }

    public void setReference(HashedUriReference reference) {
        this.reference = reference;
    }

    public Source getDataSource() {
        return dataSource;
    }

    public void setDataSource(Source dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getLocalizations() {
        return localizations;
    }

    public void setLocalizations(List<String> localizations) {
        this.localizations = localizations;
    }

    public Region getRegionOfInterest() {
        return regionOfInterest;
    }

    public void setRegionOfInterest(Region regionOfInterest) {
        this.regionOfInterest = regionOfInterest;
    }

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }
}
