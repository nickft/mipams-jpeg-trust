package org.mipams.jpegtrust.entities.assertions.enums;

public enum ShapeChoice {
    RECTANGLE("rectangle"), CIRCLE("circle"), POLYGON("polygon");

    // Store the action choice string
    private final String value;

    // Constructor
    ShapeChoice(String value) {
        this.value = value;
    }

    // Getter method to retrieve the action choice string
    public String getValue() {
        return value;
    }

}
