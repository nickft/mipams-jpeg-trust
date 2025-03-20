package org.mipams.jpegtrust.services.validation.discovery;

import org.mipams.jpegtrust.jpeg_systems.content_types.CompressedManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.StandardManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustDeclarationContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustManifestContentType;
import org.mipams.jpegtrust.jpeg_systems.content_types.UpdateManifestContentType;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManifestDiscovery {

    @Autowired
    StandardManifestContentType standardManifestContentType;

    @Autowired
    UpdateManifestContentType updateManifestContentType;

    @Autowired
    TrustDeclarationContentType trustDeclarationContentType;

    @Autowired
    CompressedManifestContentType compressedManifestContentType;

    @Autowired
    AssertionDiscovery assertionFactory;

    public TrustManifestContentType discoverManifestType(JumbfBox manifestJumbfBox)
            throws MipamsException {

        TrustManifestContentType manifestContentType;

        String descriptionUuid = manifestJumbfBox.getDescriptionBox().getUuid();

        if (updateManifestContentType.getContentTypeUuid().equals(descriptionUuid)) {
            manifestContentType = updateManifestContentType;
        } else if (trustDeclarationContentType.getContentTypeUuid().equals(descriptionUuid)) {
            manifestContentType = trustDeclarationContentType;
        } else {
            manifestContentType = standardManifestContentType;
        }

        return manifestContentType;
    }

    public boolean isUpdateManifestRequest(TrustManifestContentType manifestContentType) {
        return UpdateManifestContentType.class.equals(manifestContentType.getClass());
    }

    public boolean isStandardManifestRequest(TrustManifestContentType manifestContentType) {
        return StandardManifestContentType.class.equals(manifestContentType.getClass());
    }

    public boolean isTrustDeclarationContentType(TrustManifestContentType manifestContentType) {
        return TrustDeclarationContentType.class.equals(manifestContentType.getClass());
    }
}
