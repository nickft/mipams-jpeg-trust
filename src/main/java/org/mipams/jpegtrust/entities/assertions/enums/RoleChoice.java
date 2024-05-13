package org.mipams.jpegtrust.entities.assertions.enums;

public enum RoleChoice {
    C2PA_AREA_OF_INTEREST("c2pa.areaOfInterest"),
    C2PA_CROPPED("c2pa.cropped"),
    C2PA_EDITED("c2pa.edited"),
    C2PA_PLACED("c2pa.placed"),
    C2PA_REDACTED("c2pa.redacted"),
    C2PA_SUBJECT_AREA("c2pa.subjectArea"),
    C2PA_DELETED("c2pa.deleted"),
    C2PA_STYLED("c2pa.styled");

    // Store the role choice string
    private final String value;

    // Constructor
    RoleChoice(String value) {
        this.value = value;
    }

    // Getter method to retrieve the action choice string
    public String getValue() {
        return value;
    }
}
