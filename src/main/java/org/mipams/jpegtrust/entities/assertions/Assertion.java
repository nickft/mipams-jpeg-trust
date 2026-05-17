package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jpegtrust.entities.ProvenanceEntity;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Assertion extends ProvenanceEntity {
    public JumbfBox toJumbfBox() throws MipamsException;

    @JsonIgnore
    public String getLabel();
    
    @JsonIgnore
    default void setLabel(String label) {
        // No hace nada por defecto
    }


    @JsonIgnore
    default String getDefaultLabel() {
        return null;
    }

    @JsonIgnore
    public boolean isReductable() throws MipamsException;
}
