package org.mipams.jpegtrust.entities.assertions.region;

public class TextSelectorRange {
    TextSelector selector;
    TextSelector end;

    public TextSelector getSelector() {
        return selector;
    }

    public void setSelector(TextSelector selector) {
        this.selector = selector;
    }

    public TextSelector getEnd() {
        return end;
    }

    public void setEnd(TextSelector end) {
        this.end = end;
    }

    public class TextSelector {
        private String fragment;
        private Integer start;
        private Integer end;

        public String getFragment() {
            return fragment;
        }

        public void setFragment(String fragment) {
            this.fragment = fragment;
        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getEnd() {
            return end;
        }

        public void setEnd(Integer end) {
            this.end = end;
        }
    }
}
