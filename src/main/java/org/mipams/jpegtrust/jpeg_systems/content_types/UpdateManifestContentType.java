package org.mipams.jpegtrust.jpeg_systems.content_types;

import org.springframework.stereotype.Service;

@Service
public class UpdateManifestContentType extends TrustManifestContentType {

    @Override
    public String getContentTypeUuid() {
        return "6332756D-0011-0010-8000-00AA00389B71";
    }
}