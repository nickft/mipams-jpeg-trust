package org.mipams.jpegtrust.jpeg_systems.content_types;

import org.mipams.jumbf.entities.JumbfBox;
import org.springframework.stereotype.Service;

@Service
public class CompressedManifestContentType extends TrustManifestContentType {

    @Override
    public String getContentTypeUuid() {
        return "6332636D-0011-0010-8000-00AA00389B71";
    }

    @Override
    public void validateTypeOfAssertions(JumbfBox activeManifestJumbfBox) {
    }
}
