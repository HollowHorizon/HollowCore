package ru.hollowhorizon.hc.core;

import org.objectweb.asm.Type;

public class Boxer {

    final static public String INTERNAL_NAME = Type.getInternalName(Boxer.class);

    public static Object box(boolean v) {
        return v;
    }

    public static Object box(byte v) {
        return v;
    }

    public static Object box(char v) {
        return v;
    }

    public static Object box(short v) {
        return v;
    }

    public static Object box(int v) {
        return v;
    }

    public static Object box(long v) {
        return v;
    }

    public static Object box(float v) {
        return v;
    }

    public static Object box(double v) {
        return v;
    }
}
