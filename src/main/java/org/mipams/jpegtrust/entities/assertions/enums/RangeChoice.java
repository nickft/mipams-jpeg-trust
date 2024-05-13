package org.mipams.jpegtrust.entities.assertions.enums;

public enum RangeChoice {
    SPATIAL("spatial"),
    TEMPORAL("temporal"),
    FRAME("frame"),
    TEXTUAL("textual");

    // Store the role choice string
    private final String value;

    // Constructor
    RangeChoice(String value) {
        this.value = value;
    }

    // Getter method to retrieve the action choice string
    public String getValue() {
        return value;
    }
}
