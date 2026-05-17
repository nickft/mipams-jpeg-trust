package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jumbf.util.MipamsException;
import org.springframework.http.MediaType;

/**
 * C2PA Thumbnail assertion (spec §18.13).
 *
 * A thumbnail assertion is technically identical to an embedded data assertion
 * (§18.12) but carries a specific label identifying the use case:
 *   - {@code c2pa.thumbnail.claim}      – created at claim creation time
 *   - {@code c2pa.thumbnail.ingredient} – created when importing an ingredient
 *
 * Per §18.13.1.3: "A thumbnail assertion is an embedded data assertion but
 * with a special label identifying this specific use case."
 *
 * Therefore this class extends {@link EmbeddedFileContentTypeAssertion} and
 * does NOT use CBOR encoding.
 *
 * JUMBF structure:
 *   JumbfBox  (EmbeddedFileContentType)
 *     ├── EmbeddedFileDescriptionBox  (bfdb)  – media type, optional filename
 *     └── BinaryDataBox               (bidb)  – raw image bytes
 */
public class ThumbnailAssertion extends EmbeddedFileContentTypeAssertion {

    private static final String CLAIM_LABEL      = "c2pa.thumbnail.claim";
    private static final String INGREDIENT_LABEL = "c2pa.thumbnail.ingredient";

    /** When {@code true} (default) the label is {@code c2pa.thumbnail.claim}. */
    private boolean isClaimCreationTime = true;

    private MediaType mediaType = MediaType.IMAGE_JPEG;
    private String    fileName;
    private byte[]    data;

    // -----------------------------------------------------------------------
    // Label / use-case control
    // -----------------------------------------------------------------------

    /** Select the claim-creation thumbnail label ({@code c2pa.thumbnail.claim}). */
    public void setIsClaimCreationTime() {
        this.isClaimCreationTime = true;
    }

    /** Select the ingredient thumbnail label ({@code c2pa.thumbnail.ingredient}). */
    public void setIsClaimIngredientThumbnail() {
        this.isClaimCreationTime = false;
    }

    // -----------------------------------------------------------------------
    // Assertion / EmbeddedFileContentTypeAssertion implementation
    // -----------------------------------------------------------------------

    @Override
    public String getDefaultLabel() {
        return CLAIM_LABEL;
    }

    @Override
    public String getLabel() {
        return isClaimCreationTime ? CLAIM_LABEL : INGREDIENT_LABEL;
    }

    /** Label is derived from {@link #setIsClaimCreationTime()} /
     *  {@link #setIsClaimIngredientThumbnail()}; calling this setter has no effect. */
    @Override
    public void setLabel(String label) {
        // intentionally ignored – label is controlled by isClaimCreationTime flag
    }

    @Override
    public String getMediaType() {
        return mediaType.toString();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    // -----------------------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------------------

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}