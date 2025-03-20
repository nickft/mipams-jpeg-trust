package org.mipams.jpegtrust.entities;

import org.mipams.jumbf.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HashedUriReference implements Comparable<HashedUriReference> {

    public final static String SUPPORTED_HASH_ALGORITHM = "SHA-256";

    public HashedUriReference() {

    }

    public HashedUriReference(byte[] digest, String uri, String algorithm) {
        setDigest(digest);
        setUrl(uri);
        setAlgorithm(algorithm);
    }

    @JsonProperty("hash")
    private byte[] digest;

    private String url;

    @JsonProperty("alg")
    private String algorithm;

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String uri) {
        this.url = uri;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public int compareTo(HashedUriReference o) {
        if (this.url.equals(o.getUrl())) {
            return CoreUtils.convertByteArrayToHex(digest).compareTo(CoreUtils.convertByteArrayToHex(o.getDigest()));
        }
        return this.url.compareTo(o.getUrl());
    }

}
