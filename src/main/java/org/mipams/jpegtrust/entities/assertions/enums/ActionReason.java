package org.mipams.jpegtrust.entities.assertions.enums;

import java.util.regex.Pattern;

public enum ActionReason {
    C2PA_PII_PRESENT("c2pa.PII.present"),
    C2PA_INVALID_DATA("c2pa.invalid.data"),
    C2PA_TRADESECRET_PRESENT("c2pa.tradesecret.present"),
    C2PA_GOVERNMENT_CONFIDENTIAL("c2pa.government.confidential");

    private static final Pattern REGEX_PATTERN = Pattern.compile("([\\da-zA-Z_-]+\\.)+[\\da-zA-Z_-]+");

    // Store the action reason string
    private final String value;

    // Constructor
    ActionReason(String value) {
        this.value = value;
    }

    // Getter method to retrieve the action reason string
    public String getValue() {
        return value;
    }

    public static boolean isCompliant(String input) {
        return REGEX_PATTERN.matcher(input).matches();
    }
}