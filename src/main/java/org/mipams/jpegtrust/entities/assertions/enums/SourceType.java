package org.mipams.jpegtrust.entities.assertions.enums;

public enum SourceType {
    SIGNER("signer"),
    CLAIM_GENERATOR_REE("claimGenerator.REE"),
    CLAIM_GENERATOR_TEE("claimGenerator.TEE"),
    LOCAL_PROVIDER_REE("localProvider.REE"),
    LOCAL_PROVIDER_TEE("localProvider.TEE"),
    REMOTE_PROVIDER_1ST_PARTY("remoteProvider.1stParty"),
    REMOTE_PROVIDER_3RD_PARTY("remoteProvider.3rdParty"),
    HUMAN_ENTRY_ANONYMOUS("humanEntry.anonymous"),
    HUMAN_ENTRY_IDENTIFIED("humanEntry.identified");

    // Store the source type string
    private final String value;

    // Constructor
    SourceType(String value) {
        this.value = value;
    }

    // Getter method to retrieve the source type string
    public String getValue() {
        return value;
    }
}
