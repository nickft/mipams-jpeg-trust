package org.mipams.jpegtrust.entities;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.AssetType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HashedExtUriReference extends HashedUriReference{
    @JsonProperty("dc:format")
    private String dcFormat;
    private Integer size;
    private List<AssetType> dataTypes;

    public String getDcFormat() {
        return dcFormat;
    }

    public void setDcFormat(String dcFormat) {
        this.dcFormat = dcFormat;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<AssetType> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(List<AssetType> dataTypes) {
        this.dataTypes = dataTypes;
    }
}