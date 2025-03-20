package org.mipams.jpegtrust.entities.assertions;

public class ExclusionRange {
    int length;
    int start;

    public ExclusionRange() {

    }

    public ExclusionRange(int len, int start) {
        this.length = len;
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
