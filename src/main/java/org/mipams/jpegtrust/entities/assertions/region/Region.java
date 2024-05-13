package org.mipams.jpegtrust.entities.assertions.region;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.enums.RoleChoice;
import org.mipams.jpegtrust.entities.assertions.metadata.AssertionMetadata;

public class Region {
    List<Range> range;
    String name;
    String identifier;
    String type;
    RoleChoice role;
    String description;
    AssertionMetadata metadata;

    public List<Range> getRange() {
        return range;
    }

    public void setRange(List<Range> range) {
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RoleChoice getRole() {
        return role;
    }

    public void setRole(RoleChoice role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssertionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssertionMetadata metadata) {
        this.metadata = metadata;
    }
}
