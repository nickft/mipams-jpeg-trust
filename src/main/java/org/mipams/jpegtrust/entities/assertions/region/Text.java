package org.mipams.jpegtrust.entities.assertions.region;

import java.util.List;

public class Text {
    private List<TextSelectorRange> selectors;

    public List<TextSelectorRange> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<TextSelectorRange> selectors) {
        this.selectors = selectors;
    }
}
