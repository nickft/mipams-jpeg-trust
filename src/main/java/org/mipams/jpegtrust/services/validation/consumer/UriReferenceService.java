package org.mipams.jpegtrust.services.validation.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UriReferenceService {

    @Autowired
    CoreGeneratorService coreGeneratorService;

    public void verifyManifestUriReference(List<JumbfBox> jumbfBoxes,
            HashedUriReference targetUriReference) throws MipamsException {

        if (!targetUriReference.getAlgorithm()
                .equals(HashedUriReference.SUPPORTED_HASH_ALGORITHM)) {
            throw new MipamsException(ValidationCode.ALGORITHM_UNSUPPORTED.getCode());
        }

        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                for (JumbfBox jumbfBox : jumbfBoxes) {
                    String label = jumbfBox.getDescriptionBox().getLabel();

                    if (label == null) {
                        throw new MipamsException(
                                String.format(ValidationCode.MANIFEST_INACCESSIBLE.getCode()));
                    }

                    baos.write(getManifestSha256Digest(jumbfBox));
                }

                if (!Arrays.equals(targetUriReference.getDigest(), baos.toByteArray())) {
                    throw new MipamsException(
                            String.format(ValidationCode.ASSERTION_HASHED_URI_MISMATCH.getCode(),
                                    targetUriReference.getUrl()));
                }
            }
        } catch (IOException e) {
            throw new MipamsException(e);
        }

    }

    public byte[] getManifestSha256Digest(JumbfBox manifestJumbfBox) throws MipamsException {
        String tempFile = CoreUtils.randomStringGenerator();
        String tempFilePath = CoreUtils.createTempFile(tempFile, "jumbf");
        try {
            coreGeneratorService.generateJumbfMetadataToFile(List.of(manifestJumbfBox),
                    tempFilePath);
            return JpegTrustUtils.computeSha256DigestOfFileContents(tempFilePath);
        } finally {
            CoreUtils.deleteFile(tempFilePath);
        }
    }

}
