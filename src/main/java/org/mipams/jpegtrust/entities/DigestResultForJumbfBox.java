package org.mipams.jpegtrust.entities;

public class DigestResultForJumbfBox {
    private byte[] digest;
    private String algorithm;

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
