package org.mipams.jpegtrust.entities.assertions.enums;

public enum UnitChoice {
    PIXEL("pixel"), PERCENT("percent");

    // Store the action choice string
    private final String value;

    // Constructor
    UnitChoice(String value) {
        this.value = value;
    }

    // Getter method to retrieve the action choice string
    public String getValue() {
        return value;
    }

}
