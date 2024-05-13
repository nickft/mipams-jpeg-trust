package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jpegtrust.entities.ProvenanceEntity;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;


public interface Assertion extends ProvenanceEntity {
    public JumbfBox toJumbfBox() throws MipamsException;

    public String getLabel();
}
