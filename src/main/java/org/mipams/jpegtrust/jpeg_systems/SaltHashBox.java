package org.mipams.jpegtrust.jpeg_systems;

import org.mipams.jumbf.entities.MemoryBox;

public class SaltHashBox extends MemoryBox{
    public static int TYPE_ID = 0x63327368;
    public static String TYPE = "c2sh";

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "C2SH(" + super.toString() + ")";
    }
}
