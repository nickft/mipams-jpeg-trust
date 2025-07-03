package org.mipams.jpegtrust.entities.validation.trustindicators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mipams.jpegtrust.entities.assertions.Assertion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ManifestIndicators implements ManifestIndicatorsInterface {

    String label;

    Map<String, Assertion> assertions = new HashMap<>();

    ClaimIndicatorsInterface claim;

    @JsonProperty("claim_signature")
    ClaimSignatureIndicators claimSignature;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Assertion> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Assertion> assertions) {
        this.assertions = assertions;
    }

    public ClaimIndicatorsInterface getClaim() {
        return claim;
    }

    public void setClaim(ClaimIndicatorsInterface claim) {
        this.claim = claim;
    }

    public ClaimSignatureIndicators getClaimSignature() {
        return claimSignature;
    }

    public void setClaimSignature(ClaimSignatureIndicators claimSignature) {
        this.claimSignature = claimSignature;
    }

    public static class ManifestIndicatorsSerializer extends JsonSerializer<ManifestIndicators> {
        @Override
        public void serialize(ManifestIndicators value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {

            gen.writeStartObject();

            gen.writeFieldName("label");
            serializers.defaultSerializeValue(value.getLabel(), gen);

            gen.writeFieldName("assertions");
            serializers.defaultSerializeValue(value.getAssertions(), gen);

            gen.writeFieldName(value.getClaim().getClaimIndicatorKeyName());
            serializers.defaultSerializeValue(value.getClaim(), gen);

            gen.writeFieldName("claim_signature");
            serializers.defaultSerializeValue(value.getClaimSignature(), gen);

            gen.writeEndObject();
        }
    }
}
