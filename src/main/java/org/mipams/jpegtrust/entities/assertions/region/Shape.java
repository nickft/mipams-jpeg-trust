package org.mipams.jpegtrust.entities.assertions.region;

import java.util.List;

import org.mipams.jpegtrust.entities.assertions.enums.ShapeChoice;
import org.mipams.jpegtrust.entities.assertions.enums.UnitChoice;

public class Shape {
    private ShapeChoice type;
    private UnitChoice unit;
    private Coordinate origin;
    private Float width;
    private Float height;
    private Boolean inside;
    private List<Coordinate> vertices;

    public ShapeChoice getType() {
        return type;
    }

    public void setType(ShapeChoice type) {
        this.type = type;
    }

    public UnitChoice getUnit() {
        return unit;
    }

    public void setUnit(UnitChoice unit) {
        this.unit = unit;
    }

    public Coordinate getOrigin() {
        return origin;
    }

    public void setOrigin(Coordinate origin) {
        this.origin = origin;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Boolean isInside() {
        return inside;
    }

    public void setInside(Boolean inside) {
        this.inside = inside;
    }

    public List<Coordinate> getVertices() {
        return vertices;
    }

    public void setVertices(List<Coordinate> vertices) {
        this.vertices = vertices;
    }

    public static class Coordinate {
        private Float x;
        private Float y;

        public Float getX() {
            return x;
        }

        public void setX(Float x) {
            this.x = x;
        }

        public Float getY() {
            return y;
        }

        public void setY(Float y) {
            this.y = y;
        }
    }
}
