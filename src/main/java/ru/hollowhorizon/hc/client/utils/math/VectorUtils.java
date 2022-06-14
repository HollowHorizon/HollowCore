package ru.hollowhorizon.hc.client.utils.math;

import net.minecraft.util.math.vector.Vector4f;

public class VectorUtils {
    public static Vector4f scale(Vector4f vector4f, float scale) {
        float x = vector4f.x() * scale;
        float y = vector4f.y() * scale;
        float z = vector4f.z() * scale;
        float w = vector4f.w() * scale;
        vector4f.set(x,y,z,w);
        return vector4f;
    }

    public static Vector4f add(Vector4f left, Vector4f right, Vector4f dest) {
        if (dest == null) {
            return new Vector4f(left.x() + right.x(), left.y() + right.y(), left.z() + right.z(), left.w() + right.w());
        } else {
            dest.set(left.x() + right.x(), left.y() + right.y(), left.z() + right.z(), left.w() + right.w());
            return dest;
        }
    }
}
