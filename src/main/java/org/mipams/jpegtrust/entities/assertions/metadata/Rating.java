package org.mipams.jpegtrust.entities.assertions.metadata;

import org.mipams.jpegtrust.entities.assertions.enums.ReviewCode;

public class Rating {
    int value;
    ReviewCode review;
    String explanation;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ReviewCode getReview() {
        return review;
    }

    public void setReview(ReviewCode review) {
        this.review = review;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
