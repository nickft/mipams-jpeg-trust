package org.mipams.jpegtrust.entities.assertions;

import java.util.regex.Pattern;

import org.mipams.jpegtrust.entities.assertions.enums.AssetTypeChoice;

public class AssetType {
    AssetTypeChoice type;
    String version;

    private static final Pattern REGEX_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
                    "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
                    "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    public static boolean isCompliant(String input) {
        return REGEX_PATTERN.matcher(input).matches();
    }
}
