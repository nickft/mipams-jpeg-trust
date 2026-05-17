package org.mipams.jpegtrust.entities.assertions;

import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.EmbeddedFileDescriptionBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.content_types.EmbeddedFileContentType;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class for C2PA assertions whose JUMBF encoding uses the
 * JUMBF Embedded File content type (ISO 19566-5, Annex B), i.e.
 * a superbox containing:
 *
 *   JumbfBox  (EmbeddedFileContentType)
 *     ├── EmbeddedFileDescriptionBox  (bfdb)  – media type + optional filename
 *     └── BinaryDataBox               (bidb)  – raw bytes via a temp file
 *
 * Per C2PA spec §18.12.2:
 *   - The EmbeddedFileDescriptionBox SHALL contain an IANA media type.
 *   - It MAY contain a filename.
 *   - The External toggle bit SHALL NOT be set.
 *   - The BinaryDataBox SHALL contain the raw bytes of the data.
 *
 * Concrete subclasses must supply:
 *   - {@link #getLabel()}       – JUMBF box label (e.g. "c2pa.thumbnail.claim")
 *   - {@link #getMediaType()}   – IANA media type string (e.g. "image/jpeg")
 *   - {@link #getFileName()}    – optional filename; return {@code null} to omit
 *   - {@link #getData()}        – raw bytes to embed
 *
 * <p><b>Lifecycle note:</b> {@link #toJumbfBox()} writes the bytes to a temp
 * file so that {@link BinaryDataBox} (which only accepts a file URL) can
 * reference them.  Call {@link #deleteTempFile()} after the JumbfBox has been
 * fully serialised to disk.</p>
 */
public abstract class EmbeddedFileContentTypeAssertion implements Assertion {

    /** Path of the temp file created during {@link #toJumbfBox()}, or {@code null}. */
    private Path tempFile;

    // -----------------------------------------------------------------------
    // Abstract accessors that subclasses must implement
    // -----------------------------------------------------------------------

    /** IANA media type of the embedded data, e.g. {@code "image/jpeg"}. */
    public abstract String getMediaType();

    /**
     * Optional filename to store in the EmbeddedFileDescriptionBox.
     * Return {@code null} (or an empty string) to omit the filename field.
     */
    public abstract String getFileName();

    /** Raw bytes to embed in the BinaryDataBox. */
    public abstract byte[] getData();

    // -----------------------------------------------------------------------
    // Assertion interface
    // -----------------------------------------------------------------------

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

    /**
     * Serialises this assertion into a JUMBF superbox using the
     * EmbeddedFileContentType, as required by C2PA spec §18.12.2.
     *
     * <p>The raw bytes returned by {@link #getData()} are written to a
     * temporary file so that {@link BinaryDataBox} can reference them via a
     * file URL.  Remember to call {@link #deleteTempFile()} once serialisation
     * to the final asset has completed.</p>
     */
    @Override
    public JumbfBox toJumbfBox() throws MipamsException {
        byte[] bytes = getData();
        if (bytes == null) {
            throw new MipamsException(
                    "EmbeddedFileContentTypeAssertion [" + getLabel() + "]: getData() returned null");
        }

        // Write bytes to a temp file (BinaryDataBox requires a file URL).
        try {
            tempFile = Files.createTempFile("c2pa_embed_", ".bin");
            Files.write(tempFile, bytes);
        } catch (IOException e) {
            throw new MipamsException(
                    "EmbeddedFileContentTypeAssertion [" + getLabel() + "]: failed to write temp file", e);
        }

        // --- EmbeddedFileDescriptionBox (bfdb) ---
        EmbeddedFileDescriptionBox efdb = new EmbeddedFileDescriptionBox();
        try {
            efdb.setMediaTypeFromString(getMediaType());
        } catch (IllegalArgumentException e) {
            throw new MipamsException(
"EmbeddedFileContentTypeAssertion [" + getLabel() + 
"]: invalid media type '" + getMediaType() + "'", e);
        }

        // Spec §18.12.2: the External toggle SHALL NOT be set.
        efdb.markFileAsInternallyReferenced();

        String fileName = getFileName();
        if (fileName != null && !fileName.isBlank()) {
            efdb.setFileName(fileName);
        } else {
            efdb.setFileName(null); // clears the filename-present toggle bit
        }

        efdb.updateFieldsBasedOnExistingData();

        // --- BinaryDataBox (bidb) ---
        BinaryDataBox bidb = new BinaryDataBox();
        bidb.setFileUrl(tempFile.toAbsolutePath().toString());
        bidb.setReferencedExternally(false);

        // --- JumbfBox (EmbeddedFileContentType) ---
        JumbfBoxBuilder builder = new JumbfBoxBuilder(new EmbeddedFileContentType());
        builder.setJumbfBoxAsRequestable();
        builder.setLabel(getLabel());
        builder.appendContentBox(efdb);
        builder.appendContentBox(bidb);
        builder.setPrivateField(JpegTrustUtils.addSaltBytes());

        return builder.getResult();
    }

    /**
     * Deletes the temporary file created during {@link #toJumbfBox()}.
     * Call this after the containing JumbfBox has been fully written to disk.
     * Safe to call even if {@link #toJumbfBox()} has not been called yet.
     */
    public void deleteTempFile() {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // best-effort cleanup
            } finally {
                tempFile = null;
            }
        }
    }
}