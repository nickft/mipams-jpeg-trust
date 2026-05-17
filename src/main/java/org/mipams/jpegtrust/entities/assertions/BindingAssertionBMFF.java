package org.mipams.jpegtrust.entities.assertions;

import java.util.Arrays;
import java.util.List;

import org.mipams.jpegtrust.entities.validation.ValidationCode;
import org.mipams.jpegtrust.entities.validation.ValidationException;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BindingAssertionBMFF extends CborAssertion {

    private List<XPathExclusion> exclusions;
    
    @JsonProperty("alg")
    private String algorithm;
    
    @JsonProperty("hash")
    private byte[] digest = new byte[32];
    
    @JsonProperty("pad")
    private byte[] padding = new byte[30];
    
    @JsonProperty("pad2")
    private byte[] padding2 = new byte[0];
    
    private String name;

    @Override
    public String getLabel() {
        return "c2pa.hash.bmff.v3";
    }

    public BindingAssertionBMFF() {
    }

    public BindingAssertionBMFF(String algorithm, byte[] padding, byte[] digest, String name) {
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

    public byte[] getPadding2() {
        return padding2;
    }

    public void setPadding2(byte[] padding) {
        this.padding2 = padding;
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

    public List<XPathExclusion> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<XPathExclusion> exclusions) {
        this.exclusions = exclusions;
    }

    public void addXPathExclusion(String xpath) {
        XPathExclusion ex = new XPathExclusion(xpath);
        if (this.exclusions == null) {
            this.exclusions = List.of(ex);
        } else {
            this.exclusions.add(ex);
        }
    }

    @Override
    public boolean isReductable() throws MipamsException {
        throw new ValidationException(ValidationCode.ASSERTION_DATA_HASH_REDACTED);
    }

    // --- Clases anidadas para mapear las exclusiones XPath del JSON ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class XPathExclusion {
        private String xpath;
        private List<XPathData> data;

        public XPathExclusion() {
        }

        public XPathExclusion(String xpath) {
            this.xpath = xpath;
        }

        public String getXpath() {
            return xpath;
        }

        public void setXpath(String xpath) {
            this.xpath = xpath;
        }

        public List<XPathData> getData() {
            return data;
        }

        public void setData(List<XPathData> data) {
            this.data = data;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class XPathData {
        private int offset;
        private byte[] value; 

        public XPathData() {
        }

        public XPathData(int offset, byte[] value) {
            this.offset = offset;
            this.value = value;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public byte[] getValue() {
            return value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }
    }
}