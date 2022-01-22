package ru.hollowhorizon.hc.client.utils.math;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class VectorHelper {
    static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    static final Vector3f Z_AXIS = new Vector3f(0.0F, 0.0F, 1.0F);

    public static Matrix4f matrix4FromLocRot(float xl, float yl, float zl, float xr, float yr, float zr) {
        Vector3f loc = new Vector3f(xl, yl, zl);
        Matrix4f part1 = new Matrix4f();
        part1.setIdentity();
        part1 = MatrixUtils.translate(loc, part1);
        MatrixUtils.rotate(zr, Z_AXIS, part1);
        MatrixUtils.rotate(yr, Y_AXIS, part1);
        MatrixUtils.rotate(xr, X_AXIS, part1);
        return part1;
    }
}
