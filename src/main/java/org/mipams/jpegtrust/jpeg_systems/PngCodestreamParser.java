package org.mipams.jpegtrust.jpeg_systems;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.ParseMetadata;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.ParserInterface;
import org.mipams.jumbf.util.JpegCodestreamException;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PngCodestreamParser implements ParserInterface {

    private static final Logger logger = Logger.getLogger(PngCodestreamParser.class.getName());

    @Autowired
    CoreParserService coreParserService;

    @Override
    public List<JumbfBox> parseMetadataFromFile(String assetUrl) throws MipamsException {
        Optional<JumbfBox> jumbfBox = parseTrustRecord(assetUrl);

        return (jumbfBox.isPresent()) ? List.of(jumbfBox.get()) : List.of();
    }

    public Optional<JumbfBox> parseTrustRecord(String assetUrl) throws MipamsException {

        String tmpDirectory = CoreUtils.createSubdirectory(CoreUtils.getTempDir(), CoreUtils.randomStringGenerator());
        ParseMetadata parseMetadata = new ParseMetadata();
        parseMetadata.setParentDirectory(tmpDirectory);

        try (InputStream is = new FileInputStream(assetUrl)) {

            handlePngSignature(is, null);

            while (is.available() > 0) {

                int chunkLength = CoreUtils.readIntFromInputStream(is);

                byte[] chunkTypeByteArray = CoreUtils.readBytesFromInputStream(is, 4);

                String chunkTypeStr = new String(chunkTypeByteArray, StandardCharsets.US_ASCII);

                logger.log(Level.FINE, chunkTypeStr);

                long bytesToSkip = chunkLength + CoreUtils.INT_BYTE_SIZE;

                if ("caBX".equals(chunkTypeStr)) {
                    InputStream boundedManifestStream = new InputStream() {
                        private int remaining = chunkLength;

                        @Override
                        public int read() throws IOException {
                            if (remaining <= 0)
                                return -1;
                            int b = is.read();
                            if (b != -1)
                                remaining--;
                            return b;
                        }

                        @Override
                        public int read(byte[] b, int off, int len) throws IOException {
                            if (remaining <= 0)
                                return -1;
                            int bytesToRead = Math.min(len, remaining);
                            int bytesRead = is.read(b, off, bytesToRead);
                            if (bytesRead != -1)
                                remaining -= bytesRead;
                            return bytesRead;
                        }

                        @Override
                        public int available() throws IOException {
                            return Math.min(is.available(), remaining);
                        }
                    };

                    BufferedInputStream bufferedManifestStream = new BufferedInputStream(boundedManifestStream);

                    Optional<JumbfBox> results = coreParserService.parseJumbfBoxFromInputStream(parseMetadata,
                            bufferedManifestStream);

                    return results;
                }

                long skipped = 0;
                while (skipped < bytesToSkip) {
                    long result = is.skip(bytesToSkip - skipped);
                    if (result == 0) {
                        break;
                    }
                    skipped += result;
                }
            }

            return Optional.empty();
        } catch (IOException | MipamsException e) {
            throw new JpegCodestreamException(e);
        }
    }

    public void handlePngSignature(InputStream is, OutputStream os) throws MipamsException {
        byte[] pngSignature = CoreUtils.readBytesFromInputStream(is, 8);

        String pngSignatureAsHex = CoreUtils.convertByteArrayToHex(pngSignature);

        if (!pngSignatureAsHex.equalsIgnoreCase("89504E470D0A1A0A")) {
            throw new MipamsException("Png Signature is missing.");
        }

        if (os != null) {
            CoreUtils.writeByteArrayToOutputStream(pngSignature, os);
        }
    }
}