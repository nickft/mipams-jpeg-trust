package org.mipams.jpegtrust.entities.validation.trustindicators;

import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;

public class EmptyAssertionIndicator implements Assertion {

    @Override
    public JumbfBox toJumbfBox() throws MipamsException {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public boolean isReductable() throws MipamsException {
        return false;
    }

}
