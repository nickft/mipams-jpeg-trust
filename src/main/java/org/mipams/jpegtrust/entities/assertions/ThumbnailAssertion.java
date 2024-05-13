package org.mipams.jpegtrust.entities.assertions;

import java.util.List;

import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.EmbeddedFileDescriptionBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.content_types.EmbeddedFileContentType;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.http.MediaType;

public class ThumbnailAssertion implements Assertion {

    private final String claimCreationThumbnailLabel = "c2pa.thumbnail.claim";
    private final String ingredientlThumbnailLabel = "c2pa.thumbnail.ingredient";

    boolean isClaimCreationTime = true;
    String thumbnailUrl;
    MediaType mediaType = MediaType.IMAGE_JPEG;

    public void setIsClaimCreationTime() {
        this.isClaimCreationTime = true;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setIsClaimIngredientThumbnail() {
        this.isClaimCreationTime = false;
    }

    @Override
    public JumbfBox toJumbfBox() throws MipamsException {
        return toEmbeddedFileJumbfBox();
    }

    @Override
    public String getLabel() {
        return this.isClaimCreationTime ? claimCreationThumbnailLabel : ingredientlThumbnailLabel;
    }
    
    private JumbfBox toEmbeddedFileJumbfBox() throws MipamsException {
        JumbfBoxBuilder thumbnailJumbfBox = new JumbfBoxBuilder(new EmbeddedFileContentType());
        thumbnailJumbfBox.setJumbfBoxAsRequestable();
        thumbnailJumbfBox.setLabel(String.format("%s.%s",getLabel(), this.mediaType.getSubtype()));

        EmbeddedFileDescriptionBox efdb = new EmbeddedFileDescriptionBox();
        efdb.setMediaType(MediaType.IMAGE_JPEG);
        efdb.markFileAsInternallyReferenced();

        BinaryDataBox efbd = new BinaryDataBox();
        efbd.setFileUrl(this.thumbnailUrl); 


        thumbnailJumbfBox.appendAllContentBoxes(List.of(efdb, efbd));
        thumbnailJumbfBox.setPrivateField(JpegTrustUtils.addSaltBytes());

        return thumbnailJumbfBox.getResult();
    }
}
