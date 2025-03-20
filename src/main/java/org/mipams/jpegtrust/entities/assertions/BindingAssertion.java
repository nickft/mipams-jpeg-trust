package org.mipams.jpegtrust.entities.assertions;

import java.util.Arrays;
import java.util.List;

import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BindingAssertion extends CborAssertion {

    private List<ExclusionRange> exclusions;
    @JsonProperty("alg")
    private String algorithm;
    @JsonProperty("hash")
    private byte[] digest = new byte[32];
    @JsonProperty("pad")
    private byte[] padding = new byte[30];
    private String name;

    @Override
    public String getLabel() {
        return "c2pa.hash.data";
    }

    public BindingAssertion() {
    }

    public BindingAssertion(String algorithm, byte[] padding, byte[] digest, String name) {
        setAlgorithm(algorithm);
        setPadding(padding);
        setDigest(digest);
        setName(name);
        Arrays.fill(this.padding, Byte.valueOf("0"));
        Arrays.fill(this.digest, Byte.valueOf("0"));
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getPadding() {
        return padding;
    }

    public void setPadding(byte[] padding) {
        this.padding = padding;
    }

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExclusionRange> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<ExclusionRange> test) {
        this.exclusions = test;
    }

    public void addExclusionRange(int len, int start) {
        ExclusionRange ex = new ExclusionRange(len, start);
        this.exclusions = List.of(ex);
    }

    @Override
    public boolean isReductable() throws MipamsException {
        throw new ValidationException(ValidationCode.ASSERTION_DATA_HASH_REDACTED);
    }
}
