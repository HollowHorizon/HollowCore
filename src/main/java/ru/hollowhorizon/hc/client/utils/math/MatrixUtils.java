package ru.hollowhorizon.hc.client.utils.math;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import java.nio.FloatBuffer;

public class MatrixUtils {
    public static float[] getMatrix(Matrix4f matrix) {
        float[] mtrx = new float[16];

        mtrx[0] = matrix.m00;
        mtrx[1] = matrix.m01;
        mtrx[2] = matrix.m02;
        mtrx[3] = matrix.m03;
        mtrx[4] = matrix.m10;
        mtrx[5] = matrix.m11;
        mtrx[6] = matrix.m12;
        mtrx[7] = matrix.m13;
        mtrx[8] = matrix.m20;
        mtrx[9] = matrix.m21;
        mtrx[10] = matrix.m22;
        mtrx[11] = matrix.m23;
        mtrx[12] = matrix.m30;
        mtrx[13] = matrix.m31;
        mtrx[14] = matrix.m32;
        mtrx[15] = matrix.m33;

        return mtrx;
    }

    public static Matrix4f mul(Matrix4f first, Matrix4f second, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
            dest.setIdentity();
        }

        float m00 = first.m00 * second.m00 + first.m10 * second.m01 + first.m20 * second.m02 + first.m30 * second.m03;
        float m01 = first.m01 * second.m00 + first.m11 * second.m01 + first.m21 * second.m02 + first.m31 * second.m03;
        float m02 = first.m02 * second.m00 + first.m12 * second.m01 + first.m22 * second.m02 + first.m32 * second.m03;
        float m03 = first.m03 * second.m00 + first.m13 * second.m01 + first.m23 * second.m02 + first.m33 * second.m03;
        float m10 = first.m00 * second.m10 + first.m10 * second.m11 + first.m20 * second.m12 + first.m30 * second.m13;
        float m11 = first.m01 * second.m10 + first.m11 * second.m11 + first.m21 * second.m12 + first.m31 * second.m13;
        float m12 = first.m02 * second.m10 + first.m12 * second.m11 + first.m22 * second.m12 + first.m32 * second.m13;
        float m13 = first.m03 * second.m10 + first.m13 * second.m11 + first.m23 * second.m12 + first.m33 * second.m13;
        float m20 = first.m00 * second.m20 + first.m10 * second.m21 + first.m20 * second.m22 + first.m30 * second.m23;
        float m21 = first.m01 * second.m20 + first.m11 * second.m21 + first.m21 * second.m22 + first.m31 * second.m23;
        float m22 = first.m02 * second.m20 + first.m12 * second.m21 + first.m22 * second.m22 + first.m32 * second.m23;
        float m23 = first.m03 * second.m20 + first.m13 * second.m21 + first.m23 * second.m22 + first.m33 * second.m23;
        float m30 = first.m00 * second.m30 + first.m10 * second.m31 + first.m20 * second.m32 + first.m30 * second.m33;
        float m31 = first.m01 * second.m30 + first.m11 * second.m31 + first.m21 * second.m32 + first.m31 * second.m33;
        float m32 = first.m02 * second.m30 + first.m12 * second.m31 + first.m22 * second.m32 + first.m32 * second.m33;
        float m33 = first.m03 * second.m30 + first.m13 * second.m31 + first.m23 * second.m32 + first.m33 * second.m33;

        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m03 = m03;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m13 = m13;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m23 = m23;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.m33 = m33;

        return dest;
    }

    public static Matrix4f mul(Matrix4f first, Matrix4f second) {
        return mul(first, second, new Matrix4f());
    }

    public static Matrix4f invert(Matrix4f src, Matrix4f dest) {
        float determinant = determinant(src);

        if (determinant != 0) {
            if (dest == null) {
                dest = new Matrix4f();
                dest.setIdentity();
            }
            float determinant_inv = 1f / determinant;

            // первый ряд
            float t00 = determinant3x3(src.m11, src.m12, src.m13, src.m21, src.m22, src.m23, src.m31, src.m32, src.m33);
            float t01 = -determinant3x3(src.m10, src.m12, src.m13, src.m20, src.m22, src.m23, src.m30, src.m32, src.m33);
            float t02 = determinant3x3(src.m10, src.m11, src.m13, src.m20, src.m21, src.m23, src.m30, src.m31, src.m33);
            float t03 = -determinant3x3(src.m10, src.m11, src.m12, src.m20, src.m21, src.m22, src.m30, src.m31, src.m32);
            // второй ряд
            float t10 = -determinant3x3(src.m01, src.m02, src.m03, src.m21, src.m22, src.m23, src.m31, src.m32, src.m33);
            float t11 = determinant3x3(src.m00, src.m02, src.m03, src.m20, src.m22, src.m23, src.m30, src.m32, src.m33);
            float t12 = -determinant3x3(src.m00, src.m01, src.m03, src.m20, src.m21, src.m23, src.m30, src.m31, src.m33);
            float t13 = determinant3x3(src.m00, src.m01, src.m02, src.m20, src.m21, src.m22, src.m30, src.m31, src.m32);
            // третий ряд
            float t20 = determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m31, src.m32, src.m33);
            float t21 = -determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m30, src.m32, src.m33);
            float t22 = determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m30, src.m31, src.m33);
            float t23 = -determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m30, src.m31, src.m32);
            // fourth row
            float t30 = -determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m21, src.m22, src.m23);
            float t31 = determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m20, src.m22, src.m23);
            float t32 = -determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m20, src.m21, src.m23);
            float t33 = determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m20, src.m21, src.m22);

            // transpose and divide by the determinant
            dest.m00 = t00 * determinant_inv;
            dest.m11 = t11 * determinant_inv;
            dest.m22 = t22 * determinant_inv;
            dest.m33 = t33 * determinant_inv;
            dest.m01 = t10 * determinant_inv;
            dest.m10 = t01 * determinant_inv;
            dest.m20 = t02 * determinant_inv;
            dest.m02 = t20 * determinant_inv;
            dest.m12 = t21 * determinant_inv;
            dest.m21 = t12 * determinant_inv;
            dest.m03 = t30 * determinant_inv;
            dest.m30 = t03 * determinant_inv;
            dest.m13 = t31 * determinant_inv;
            dest.m31 = t13 * determinant_inv;
            dest.m32 = t23 * determinant_inv;
            dest.m23 = t32 * determinant_inv;
            return dest;
        } else
            return null;
    }

    private static float determinant3x3(float t00, float t01, float t02, float t10, float t11, float t12, float t20, float t21, float t22) {
        return t00 * (t11 * t22 - t12 * t21)
                + t01 * (t12 * t20 - t10 * t22)
                + t02 * (t10 * t21 - t11 * t20);
    }

    public static float determinant(Matrix4f matrix) {
        float f =
                matrix.m00
                        * ((matrix.m11 * matrix.m22 * matrix.m33 + matrix.m12 * matrix.m23 * matrix.m31 + matrix.m13 * matrix.m21 * matrix.m32)
                        - matrix.m13 * matrix.m22 * matrix.m31
                        - matrix.m11 * matrix.m23 * matrix.m32
                        - matrix.m12 * matrix.m21 * matrix.m33);
        f -= matrix.m01
                * ((matrix.m10 * matrix.m22 * matrix.m33 + matrix.m12 * matrix.m23 * matrix.m30 + matrix.m13 * matrix.m20 * matrix.m32)
                - matrix.m13 * matrix.m22 * matrix.m30
                - matrix.m10 * matrix.m23 * matrix.m32
                - matrix.m12 * matrix.m20 * matrix.m33);
        f += matrix.m02
                * ((matrix.m10 * matrix.m21 * matrix.m33 + matrix.m11 * matrix.m23 * matrix.m30 + matrix.m13 * matrix.m20 * matrix.m31)
                - matrix.m13 * matrix.m21 * matrix.m30
                - matrix.m10 * matrix.m23 * matrix.m31
                - matrix.m11 * matrix.m20 * matrix.m33);
        f -= matrix.m03
                * ((matrix.m10 * matrix.m21 * matrix.m32 + matrix.m11 * matrix.m22 * matrix.m30 + matrix.m12 * matrix.m20 * matrix.m31)
                - matrix.m12 * matrix.m21 * matrix.m30
                - matrix.m10 * matrix.m22 * matrix.m31
                - matrix.m11 * matrix.m20 * matrix.m32);
        return f;
    }

    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f dest) {
        return rotate(angle, axis, dest, dest);
    }

    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
            dest.setIdentity();
        }
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float oneminusc = 1.0f - c;
        float xy = axis.x() * axis.y();
        float yz = axis.y() * axis.z();
        float xz = axis.x() * axis.z();
        float xs = axis.x() * s;
        float ys = axis.y() * s;
        float zs = axis.z() * s;

        float f00 = axis.x() * axis.x() * oneminusc + c;
        float f01 = xy * oneminusc + zs;
        float f02 = xz * oneminusc - ys;
        // n[3] not used
        float f10 = xy * oneminusc - zs;
        float f11 = axis.y() * axis.y() * oneminusc + c;
        float f12 = yz * oneminusc + xs;
        // n[7] not used
        float f20 = xz * oneminusc + ys;
        float f21 = yz * oneminusc - xs;
        float f22 = axis.z() * axis.z() * oneminusc + c;

        float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
        float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
        float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
        float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
        float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
        float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
        float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
        float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
        dest.m20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
        dest.m21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
        dest.m22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
        dest.m23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;
        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        return dest;
    }

    public static void scale(Vector3f vec, Matrix4f src) {
        src.m00 = src.m00 * vec.x();
        src.m01 = src.m01 * vec.x();
        src.m02 = src.m02 * vec.x();
        src.m03 = src.m03 * vec.x();
        src.m10 = src.m10 * vec.y();
        src.m11 = src.m11 * vec.y();
        src.m12 = src.m12 * vec.y();
        src.m13 = src.m13 * vec.y();
        src.m20 = src.m20 * vec.z();
        src.m21 = src.m21 * vec.z();
        src.m22 = src.m22 * vec.z();
        src.m23 = src.m23 * vec.z();
    }

    public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f dest) {
        if (dest == null) {
            dest = new Vector4f();
        }
        float x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30 * right.w;
        float y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31 * right.w;
        float z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32 * right.w;
        float w = left.m03 * right.x + left.m13 * right.y + left.m23 * right.z + left.m33 * right.w;

        dest.x = x;
        dest.y = y;
        dest.z = z;
        dest.w = w;

        return dest;
    }

    public static Matrix4f translate(Vector3f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();

        dest.m30 += src.m00 * vec.x() + src.m10 * vec.y() + src.m20 * vec.z();
        dest.m31 += src.m01 * vec.x() + src.m11 * vec.y() + src.m21 * vec.z();
        dest.m32 += src.m02 * vec.x() + src.m12 * vec.y() + src.m22 * vec.z();
        dest.m33 += src.m03 * vec.x() + src.m13 * vec.y() + src.m23 * vec.z();

        return dest;
    }

    public static Matrix4f translate(Vector3f vec, Matrix4f dest) {
        return translate(vec, dest, dest);
    }


    public static void store(Matrix4f matrix, FloatBuffer buf) {
        buf.put(matrix.m00);
        buf.put(matrix.m01);
        buf.put(matrix.m02);
        buf.put(matrix.m03);
        buf.put(matrix.m10);
        buf.put(matrix.m11);
        buf.put(matrix.m12);
        buf.put(matrix.m13);
        buf.put(matrix.m20);
        buf.put(matrix.m21);
        buf.put(matrix.m22);
        buf.put(matrix.m23);
        buf.put(matrix.m30);
        buf.put(matrix.m31);
        buf.put(matrix.m32);
        buf.put(matrix.m33);
    }
}
