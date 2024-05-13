package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.content_types.CborContentType;
import org.mipams.jumbf.util.MipamsException;

public abstract class CborAssertion implements Assertion {
    
    @Override
    public JumbfBox toJumbfBox() throws MipamsException {
        return toCborJumbfBox();
    }

    private JumbfBox toCborJumbfBox() throws MipamsException {
        JumbfBoxBuilder actionJumbfBox = new JumbfBoxBuilder(new CborContentType());
        actionJumbfBox.setJumbfBoxAsRequestable();
        actionJumbfBox.setLabel(getLabel());

        CborBox cborBox = new CborBox();
        cborBox.setContent(CoseUtils.toCborEncodedByteArray(this));
        actionJumbfBox.appendContentBox(cborBox);
        actionJumbfBox.setPrivateField(JpegTrustUtils.addSaltBytes());

        return actionJumbfBox.getResult();
    }
}
