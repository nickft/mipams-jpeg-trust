package org.mipams.jpegtrust.entities.assertions.metadata;

import java.util.List;

import org.mipams.jpegtrust.entities.Actor;
import org.mipams.jpegtrust.entities.assertions.enums.SourceType;

public class Source {
    SourceType type;
    String details;
    List<Actor> actors;

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}
