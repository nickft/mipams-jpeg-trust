package org.mipams.jpegtrust.services;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;

import org.mipams.jpegtrust.entities.DigestResultForJumbfBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JumbfBoxDigestService {

    @Autowired
    CoreGeneratorService coreGeneratorService;

    public DigestResultForJumbfBox calculateDigestForJumbfBox(JumbfBox jumbfBox) throws MipamsException {
        try {
            long skipBytes = jumbfBox.isXBoxEnabled() ? 16 : 8;
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            try (DigestOutputStream dos = new DigestOutputStream(OutputStream.nullOutputStream(), sha);
                    SkippingOutputStream sos = new SkippingOutputStream(dos, skipBytes)) {

                coreGeneratorService.generateJumbfBoxesToOutputStream(List.of(jumbfBox), sos);
            }

            DigestResultForJumbfBox result = new DigestResultForJumbfBox();
            result.setDigest(sha.digest());
            result.setAlgorithm("sha256");

            return result;
        } catch (Exception e) {
            throw new MipamsException(e);
        }
    }

    private final class SkippingOutputStream extends OutputStream {

        private final OutputStream delegate;
        private long bytesToSkip;

        public SkippingOutputStream(OutputStream delegate, long bytesToSkip) {
            this.delegate = delegate;
            this.bytesToSkip = bytesToSkip;
        }

        @Override
        public void write(int b) throws IOException {
            if (bytesToSkip > 0) {
                bytesToSkip--;
            } else {
                delegate.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (bytesToSkip >= len) {
                bytesToSkip -= len;
                return;
            }

            int skip = (int) Math.min(bytesToSkip, len);
            bytesToSkip -= skip;

            delegate.write(b, off + skip, len - skip);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
