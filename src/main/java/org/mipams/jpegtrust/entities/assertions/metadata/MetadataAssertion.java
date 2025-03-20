package org.mipams.jpegtrust.entities.assertions.metadata;

import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.assertions.Assertion;
import org.mipams.jumbf.entities.JsonBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.content_types.JsonContentType;
import org.mipams.jumbf.util.MipamsException;

public class MetadataAssertion implements Assertion {

    private byte[] payload;

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String getLabel() {
        return "c2pa.metadata";
    }

    @Override
    public JumbfBox toJumbfBox() throws MipamsException {
        return toJsonJumbfBox();
    }

    private JumbfBox toJsonJumbfBox() throws MipamsException {
        JumbfBoxBuilder actionJumbfBox = new JumbfBoxBuilder(new JsonContentType());
        actionJumbfBox.setJumbfBoxAsRequestable();
        actionJumbfBox.setLabel(getLabel());

        JsonBox jsonBox = new JsonBox();
        jsonBox.setContent(getPayload());
        actionJumbfBox.appendContentBox(jsonBox);
        actionJumbfBox.setPrivateField(JpegTrustUtils.addSaltBytes());

        return actionJumbfBox.getResult();
    }

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

}
