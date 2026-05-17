package org.mipams.jpegtrust.entities.assertions.tfm;

import org.mipams.jpegtrust.entities.assertions.EmbeddedFileContentTypeAssertion;
import org.mipams.jumbf.util.MipamsException;

/**
 * C2PA {@code c2pa.embedded-data} assertion (spec §18.12).
 *
 * Uses the JUMBF Embedded File content type box, exactly as described in
 * §18.12.2:
 *
 *   JumbfBox  (EmbeddedFileContentType)
 *     ├── EmbeddedFileDescriptionBox  (bfdb)  – IANA media type + optional filename
 *     └── BinaryDataBox               (bidb)  – raw bytes of the embedded data
 *
 * Constraints (§18.12.2):
 *   - The EmbeddedFileDescriptionBox SHALL contain an IANA media type.
 *   - It MAY contain a filename.
 *   - The External toggle bit SHALL NOT be set.
 */
public class EmbeddedDataAssertion extends EmbeddedFileContentTypeAssertion {

    private String label    = getDefaultLabel();
    private String mediaType;
    private String fileName;
    private byte[] data;

    // -----------------------------------------------------------------------
    // Assertion / EmbeddedFileContentTypeAssertion implementation
    // -----------------------------------------------------------------------

    @Override
    public String getDefaultLabel() {
        return "c2pa.embedded-data";
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getMediaType() {
        return mediaType;
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

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}