package org.mipams.jpegtrust.entities.assertions.enums;

import java.util.regex.Pattern;

public enum ActionChoice {
    C2PA_COLOR_ADJUSTMENTS("c2pa.color_adjustments"),
    C2PA_CONVERTED("c2pa.converted"),
    C2PA_COPIED("c2pa.copied"),
    C2PA_CREATED("c2pa.created"),
    C2PA_CROPPED("c2pa.cropped"),
    C2PA_DRAWING("c2pa.drawing"),
    C2PA_EDITED("c2pa.edited"),
    C2PA_EDITED_METADATA("c2pa.edited.metadata"),
    C2PA_FILTERED("c2pa.filtered"),
    C2PA_FORMATTED("c2pa.formatted"),
    C2PA_MANAGED("c2pa.managed"),
    C2PA_OPENED("c2pa.opened"),
    C2PA_ORIENTATION("c2pa.orientation"),
    C2PA_PRODUCED("c2pa.produced"),
    C2PA_PLACED("c2pa.placed"),
    C2PA_PRINTED("c2pa.printed"),
    C2PA_PUBLISHED("c2pa.published"),
    C2PA_REDACTED("c2pa.redacted"),
    C2PA_REMOVED("c2pa.removed"),
    C2PA_REPACKAGED("c2pa.repackaged"),
    C2PA_RESIZED("c2pa.resized"),
    C2PA_SAVED("c2pa.saved"),
    C2PA_TRANSCODED("c2pa.transcoded"),
    C2PA_WATERMARKED("c2pa.watermarked"),
    C2PA_UNKNOWN("c2pa.unknown"),
    C2PA_VERSION_UPDATED("c2pa.version_updated"),
    FONT_EDITED("font.edited"),
    FONT_SUBSET("font.subset"),
    FONT_CREATED_FROM_VARIABLE_FONT("font.createdFromVariableFont"),
    FONT_CHARACTERS_ADDED("font.charactersAdded"),
    FONT_CHARACTERS_DELETED("font.charactersDeleted"),
    FONT_CHARACTERS_MODIFIED("font.charactersModified"),
    FONT_HINTED("font.hinted"),
    FONT_OPEN_TYPE_FEATURE_ADDED("font.openTypeFeatureAdded"),
    FONT_OPEN_TYPE_FEATURE_MODIFIED("font.openTypeFeatureModified"),
    FONT_OPEN_TYPE_FEATURE_REMOVED("font.openTypeFeatureRemoved"),
    FONT_MERGED("font.merged");

    private static final Pattern REGEX_PATTERN = Pattern.compile("([\\da-zA-Z_-]+\\.)+[\\da-zA-Z_-]+");

    private final String value;

    ActionChoice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isCompliant(String input) {
        return REGEX_PATTERN.matcher(input).matches();
    }

}