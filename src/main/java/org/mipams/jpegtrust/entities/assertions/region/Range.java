package org.mipams.jpegtrust.entities.assertions.region;

import org.mipams.jpegtrust.entities.assertions.enums.RangeChoice;

public class Range {
    RangeChoice range;
    Shape shape;

    public RangeChoice getRange() {
        return range;
    }

    public void setRange(RangeChoice range) {
        this.range = range;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
