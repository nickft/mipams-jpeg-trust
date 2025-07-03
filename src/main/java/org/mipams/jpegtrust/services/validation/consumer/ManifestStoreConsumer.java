package org.mipams.jpegtrust.services.validation.consumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.entities.validation.trustindicators.ManifestIndicators;
import org.mipams.jpegtrust.entities.validation.trustindicators.TrustIndicatorSet;
import org.mipams.jpegtrust.jpeg_systems.content_types.TrustManifestContentType;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery;
import org.mipams.jpegtrust.services.validation.discovery.ManifestDiscovery;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManifestStoreConsumer {

    @Autowired
    ManifestConsumer manifestConsumer;

    @Autowired
    UriReferenceService uriReferenceService;

    @Autowired
    ManifestDiscovery manifestDiscovery;

    @Autowired
    AssertionDiscovery assertionFactory;

    @Autowired
    AssertionConsumer assertionConsumer;

    public TrustIndicatorSet validate(JumbfBox manifestStoreJumbfBox, String assetUrl)
            throws MipamsException {

        HashSet<String> redactedAssertions = new HashSet<>();
        Map<String, HashedUriReference> ingredientManifests = new HashMap<>();

        TrustIndicatorSet trustIndicatorSet = new TrustIndicatorSet();

        String manifestUuid = JpegTrustUtils.locateActiveManifestUuid(manifestStoreJumbfBox);

        final String manifestUuidWithEnforceableContentBinding = manifestConsumer
                .locateManifestWithEnforceableContentBinding(manifestStoreJumbfBox);

        do {

            if (ingredientManifests.size() > 0) {
                String uri = ingredientManifests.keySet().iterator().next();
                manifestUuid = JpegTrustUtils.getLabelFromManifestUri(uri);
                ingredientManifests.remove(uri);
            }

            ManifestIndicators manifestIndicators = manifestConsumer.validateManifest(manifestUuid,
                    manifestStoreJumbfBox, redactedAssertions);
            if (manifestUuid.equals(manifestUuidWithEnforceableContentBinding)) {

                try {
                    manifestConsumer.validateContentBinding(manifestUuid, manifestStoreJumbfBox,
                            assetUrl);

                    manifestIndicators.getValidationStatusIndicators()
                            .setContentStatus(ValidationCode.ASSERTION_DATA_HASH_MATCH);
                } catch (ValidationException e) {
                    manifestIndicators.getValidationStatusIndicators()
                            .setContentStatus(ValidationCode.ASSERTION_DATA_HASH_MISMATCH);
                }
            }

            if (isTrustDeclaration(manifestUuid, manifestStoreJumbfBox)) {
                trustIndicatorSet.setDeclaration(manifestIndicators);
                return trustIndicatorSet;
            }

            trustIndicatorSet.getManifests().add(manifestIndicators);

            if (manifestIndicators.getAssertions().isEmpty()) {
                continue;
            }

            redactedAssertions.addAll(manifestConsumer.extractRedactedAssertions(manifestUuid,
                    manifestStoreJumbfBox));

            ingredientManifests.putAll(
                    manifestConsumer.getIngredientManifests(manifestUuid, manifestStoreJumbfBox));

        } while (ingredientManifests.size() > 0);

        return trustIndicatorSet;
    }

    private boolean isTrustDeclaration(String manifestUuid, JumbfBox manifestStoreJumbfBox)
            throws MipamsException {
        JumbfBox currentManifest = JpegTrustUtils.locateManifestOnManifestStore(manifestUuid, manifestStoreJumbfBox);

        TrustManifestContentType trustManifestContentType = manifestDiscovery
                .discoverManifestType(currentManifest);
        return manifestDiscovery.isTrustDeclarationContentType(trustManifestContentType);
    }
}
