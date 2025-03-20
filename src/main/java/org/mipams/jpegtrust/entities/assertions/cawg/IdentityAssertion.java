package org.mipams.jpegtrust.entities.assertions.cawg;

import java.util.List;

import org.mipams.jpegtrust.entities.HashedUriReference;
import org.mipams.jpegtrust.entities.assertions.CborAssertion;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityAssertion extends CborAssertion {

    @JsonProperty("signer_payload")
    private SignerPayload signerPayload;

    @JsonProperty("signature")
    private byte[] signature;

    @JsonProperty("pad1")
    private byte[] pad1;

    @JsonProperty("pad2")
    private byte[] pad2;

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

    public SignerPayload getSignerPayload() {
        return signerPayload;
    }

    public void setSignerPayload(SignerPayload signerPayload) {
        this.signerPayload = signerPayload;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getPad1() {
        return pad1;
    }

    public void setPad1(byte[] pad1) {
        this.pad1 = pad1;
    }

    public byte[] getPad2() {
        return pad2;
    }

    public void setPad2(byte[] pad2) {
        this.pad2 = pad2;
    }

    @Override
    public String getLabel() {
        return "cawg.identity";
    }

    public class SignerPayload {

        @JsonProperty("referenced_assertions")
        private List<HashedUriReference> referencedAssertions;

        @JsonProperty("sig_type")
        private String signingType;

        @JsonProperty("role")
        private List<String> role;

        @JsonProperty("expected_partial_claim")
        private HashMap expectedPartialClaim;

        @JsonProperty("expected_claim_generator")
        private HashMap expectedClaimGenerator;

        @JsonProperty("expected_countersigners")
        private List<ExpectedCounterSigner> expectedCounterSigners;

        public List<HashedUriReference> getReferencedAssertions() {
            return referencedAssertions;
        }

        public void setReferencedAssertions(List<HashedUriReference> referencedAssertions) {
            this.referencedAssertions = referencedAssertions;
        }

        public String getSigningType() {
            return signingType;
        }

        public void setSigningType(String signingType) {
            this.signingType = signingType;
        }

        public List<String> getRole() {
            return role;
        }

        public void setRole(List<String> role) {
            this.role = role;
        }

        public HashMap getExpectedPartialClaim() {
            return expectedPartialClaim;
        }

        public void setExpectedPartialClaim(HashMap expectedPartialClaim) {
            this.expectedPartialClaim = expectedPartialClaim;
        }

        public HashMap getExpectedClaimGenerator() {
            return expectedClaimGenerator;
        }

        public void setExpectedClaimGenerator(HashMap expectedClaimGenerator) {
            this.expectedClaimGenerator = expectedClaimGenerator;
        }

        public List<ExpectedCounterSigner> getExpectedCounterSigners() {
            return expectedCounterSigners;
        }

        public void setExpectedCounterSigner(List<ExpectedCounterSigner> expectedCounterSigners) {
            this.expectedCounterSigners = expectedCounterSigners;
        }

        public class HashMap {

            @JsonProperty("alg")
            private String hashAlgorithm;

            @JsonProperty("hash")
            private byte[] digest;

            public String getHashAlgorithm() {
                return hashAlgorithm;
            }

            public void setHashAlgorithm(String hashAlgorithm) {
                this.hashAlgorithm = hashAlgorithm;
            }

            public byte[] getDigest() {
                return digest;
            }

            public void setDigest(byte[] digest) {
                this.digest = digest;
            }
        }

        public class ExpectedCounterSigner {

            @JsonProperty("partial_signer_payload")
            private SignerPayload partialSignerPayload;

            @JsonProperty("expected_credentials")
            private HashMap expectedCredentials;

            public SignerPayload getPartialSignerPayload() {
                return partialSignerPayload;
            }

            public void setPartialSignerPayload(SignerPayload partialSignerPayload) {
                this.partialSignerPayload = partialSignerPayload;
            }

            public HashMap getExpectedCredentials() {
                return expectedCredentials;
            }

            public void setExpectedCredentials(HashMap expectedCredentials) {
                this.expectedCredentials = expectedCredentials;
            }
        }
    }
}
