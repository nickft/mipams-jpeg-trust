package org.mipams.jpegtrust.entities.assertions.enums;

public enum ReviewCode {
    ACTIONS_UNKNOWN_ACTIONS_PERFORMED("actions.unknownActionsPerformed"),
    ACTIONS_MISSING("actions.missing"),
    ACTIONS_POSSIBLY_MISSING("actions.possiblyMissing"),
    DEPTH_MAP_SCENE_MISMATCH("depthMap.sceneMismatch"),
    INGREDIENT_MODIFIED("ingredient.modified"),
    INGREDIENT_POSSIBLY_MODIFIED("ingredient.possiblyModified"),
    THUMBNAIL_PRIMARY_MISMATCH("thumbnail.primaryMismatch"),
    STDS_IPTC_LOCATION_INACCURATE("stds.iptc.location.inaccurate"),
    STDS_SCHEMA_ORG_CREATIVE_WORK_MISATTRIBUTED("stds.schema-org.CreativeWork.misattributed"),
    STDS_SCHEMA_ORG_CREATIVE_WORK_MISSING_ATTRIBUTION("stds.schema-org.CreativeWork.missingAttribution");

    private final String value;

    ReviewCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
