package org.mipams.jpegtrust.services.validation.consumer;

import org.mipams.jumbf.entities.CborBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.util.MipamsException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import org.mipams.jpegtrust.cose.CoseUtils;
import org.mipams.jpegtrust.entities.Claim;
import org.mipams.jpegtrust.entities.ClaimV1;
import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.JpegTrustUtils;
import org.mipams.jpegtrust.entities.ProvenanceEntity;
import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimIndicators;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimIndicatorsInterface;
import org.mipams.jpegtrust.entities.validation.trustindicators.ClaimV1Indicators;
import org.mipams.jpegtrust.jpeg_systems.JumbfUtils;
import org.mipams.jpegtrust.jpeg_systems.content_types.ClaimContentType;
import org.mipams.jpegtrust.services.validation.discovery.AssertionDiscovery.MipamsAssertion;
import org.springframework.stereotype.Service;

@Service
public class ClaimConsumer {
    public ProvenanceEntity deserializeClaimJumbfBox(JumbfBox claimJumbfBox) throws MipamsException {

        CborBox claimCborBox = (CborBox) claimJumbfBox.getContentBoxList().get(0);
        try {

            if (claimJumbfBox.getDescriptionBox().getLabel().startsWith(new ClaimContentType().getLabelV1())) {
                return CoseUtils.toClaimV1(claimCborBox.getContent());
            } else if (claimJumbfBox.getDescriptionBox().getLabel().startsWith(new ClaimContentType().getLabel())) {
                return CoseUtils.toClaim(claimCborBox.getContent());
            } else {
                throw new ValidationException(ValidationCode.CLAIM_CBOR_MALFORMED);
            }
        } catch (Exception e) {
            throw new MipamsException(e);
        }
    }

    public ClaimIndicatorsInterface buildClaimIndicatorSet(ProvenanceEntity claim) throws ValidationException {

        if (claim.getClass().equals(ClaimV1.class)) {
            return new ClaimV1Indicators((ClaimV1) claim);
        } else if (claim.getClass().equals(Claim.class)) {
            return new ClaimIndicators((Claim) claim);
        } else {
            throw new ValidationException(ValidationCode.CLAIM_CBOR_INVALID);
        }
    }

    public HashSet<String> extractRedactedAssertions(JumbfBox manifestJumbfBox) throws MipamsException {
        JumbfBox claimJumbfBox = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(manifestJumbfBox,
                new ClaimContentType());
        ProvenanceEntity claim = deserializeClaimJumbfBox(claimJumbfBox);

        HashSet<String> result = new HashSet<>();

        if (claim.getClass().equals(ClaimV1.class)) {
            ClaimV1 claimV1 = (ClaimV1) claim;
            if (claimV1.getRedactedAssertions() != null) {
                result.addAll(claimV1.getRedactedAssertions());
            }
        } else if (claim.getClass().equals(Claim.class)) {
            Claim claimV2 = (Claim) claim;
            if (claimV2.getRedactedAssertions() != null) {
                result.addAll(claimV2.getRedactedAssertions());
            }
        } else {
            throw new ValidationException(ValidationCode.CLAIM_CBOR_INVALID);
        }

        String manifestLabel = manifestJumbfBox.getDescriptionBox().getLabel();

        boolean selfRedactedAssertions = result.stream()
                .filter(uri -> uri.contains(
                        JpegTrustUtils.getProvenanceJumbfURL(manifestLabel)) || !JumbfUtils.isJumbfUriAbsolute(uri))
                .count() > 0;

        if (selfRedactedAssertions) {
            throw new ValidationException(ValidationCode.ASSERTION_SELF_REDACTED);
        }

        return result;
    }

    public LinkedHashSet<HashedUriReference> getNonRedactedAssertionsFromClaimInManifestJumbfBox(
            JumbfBox manifestJumbfBox, HashSet<String> redactedAssertions) throws MipamsException {
        final String manifestUuid = manifestJumbfBox.getDescriptionBox().getLabel();
        JumbfBox claimJumbfBox = JpegTrustUtils.locateJpegTrustJumbfBoxByContentType(manifestJumbfBox,
                new ClaimContentType());
        ProvenanceEntity claim = deserializeClaimJumbfBox(claimJumbfBox);

        LinkedHashSet<HashedUriReference> result = new LinkedHashSet<>();

        if (claim.getClass().equals(ClaimV1.class)) {
            result.addAll(((ClaimV1) claim).getAssertions());
        } else if (claim.getClass().equals(Claim.class)) {
            result.addAll(((Claim) claim).getCreatedAssertions());
            result.addAll(((Claim) claim).getGatheredAssertions());
        } else {
            throw new ValidationException(ValidationCode.CLAIM_CBOR_INVALID);
        }

        for (HashedUriReference ref : new TreeSet<>(result)) {
            if (ref.getUrl().startsWith("self#jumbf=/")) {
                if (!ref.getUrl().contains(manifestUuid)) {
                    throw new ValidationException(ValidationCode.ASSERTION_OUTSIDE_MANIFEST);
                }
            }

            String absolutePath = (JumbfUtils.isJumbfUriAbsolute(ref.getUrl())) ? ref.getUrl()
                    : JpegTrustUtils.getProvenanceJumbfURL(manifestUuid, ref.getUrl());

            if (!redactedAssertions.contains(absolutePath)) {
                continue;
            }

            MipamsAssertion assertionType = MipamsAssertion
                    .getTypeFromLabel(JpegTrustUtils.getLabelFromManifestUri(absolutePath));

            if (assertionType == null) {
                continue;
            }

            if (assertionType.isRedactableOrThrowException()) {
                result.remove(ref);
            }
        }

        return result;
    }
}
