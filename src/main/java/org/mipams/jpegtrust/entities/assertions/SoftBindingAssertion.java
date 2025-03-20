package org.mipams.jpegtrust.entities.assertions;

import java.util.ArrayList;
import java.util.List;

import org.mipams.jpegtrust.entities.assertions.region.Region;
import org.mipams.jumbf.util.MipamsException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SoftBindingAssertion extends CborAssertion {

    @JsonProperty("alg")
    private String algorithm;

    @JsonProperty("blocks")
    private List<SoftBindingBlock> blocks = new ArrayList<>();

    @JsonProperty("pad")
    private byte[] padding = new byte[30];

    @JsonProperty("pad2")
    private byte[] padding2;

    private String name;

    @JsonProperty("alg-params")
    private String algorithmParams;

    @Override
    public String getLabel() {
        return "c2pa.soft-binding";
    }

    public SoftBindingAssertion() {
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

    public void setPadding2(byte[] padding2) {
        this.padding2 = padding2;
    }

    public List<SoftBindingBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<SoftBindingBlock> blocks) {
        this.blocks = blocks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlgorithmParams() {
        return algorithmParams;
    }

    public void setAlgorithmParams(String algorithmParams) {
        this.algorithmParams = algorithmParams;
    }

    @Override
    public boolean isReductable() throws MipamsException {
        return true;
    }

    public class SoftBindingBlock {
        private String extent;
        private SoftBindingTimespan timespan;
        private Region region;

        public String getExtent() {
            return extent;
        }

        public void setExtent(String extent) {
            this.extent = extent;
        }

        public SoftBindingTimespan getTimespan() {
            return timespan;
        }

        public void setTimespan(SoftBindingTimespan timespan) {
            this.timespan = timespan;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }

    }

    public class SoftBindingTimespan {
        private int start;
        private int end;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

    }
}
