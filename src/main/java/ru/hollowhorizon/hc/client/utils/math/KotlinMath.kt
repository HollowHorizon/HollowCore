package ru.hollowhorizon.hc.client.utils.math

import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun Matrix4f.translationRotateScale(
    tx: Float, ty: Float, tz: Float,
    qx: Float, qy: Float, qz: Float, qw: Float,
    sx: Float, sy: Float, sz: Float
): Matrix4f {
    val dqx = qx + qx
    val dqy = qy + qy
    val dqz = qz + qz
    val q00 = dqx * qx
    val q11 = dqy * qy
    val q22 = dqz * qz
    val q01 = dqx * qy
    val q02 = dqx * qz
    val q03 = dqx * qw
    val q12 = dqy * qz
    val q13 = dqy * qw
    val q23 = dqz * qw
    this.m00 = sx - (q11 + q22) * sx
    this.m01 = (q01 + q23) * sx
    this.m02 = (q02 - q13) * sx
    this.m03 = 0.0f
    this.m10 = (q01 - q23) * sy
    this.m11 = sy - (q22 + q00) * sy
    this.m12 = (q12 + q03) * sy
    this.m13 = 0.0f
    this.m20 = (q02 + q13) * sz
    this.m21 = (q12 - q03) * sz
    this.m22 = sz - (q11 + q00) * sz
    this.m23 = 0.0f
    this.m30 = tx
    this.m31 = ty
    this.m32 = tz
    this.m33 = 1.0f
    return this
}

fun Matrix4f.translationRotateScale(pos: Vector3f, rot: Quaternion, scale: Float): Matrix4f {
    return this.translationRotateScale(
        pos.x(), pos.y(), pos.z(),
        rot.i(), rot.j(), rot.k(), rot.r(),
        scale, scale, scale
    )
}

fun Matrix4f.writeTo(buffer: ByteBuffer) {
    val pos = buffer.position()

    if (pos == 0) {
        put0(this, buffer)
    } else {
        putN(this, pos, buffer)
    }
}

private fun put0(m: Matrix4f, dest: ByteBuffer) {
    dest.putFloat(0, m.m00)
        .putFloat(4, m.m01)
        .putFloat(8, m.m02)
        .putFloat(12, m.m03)
        .putFloat(16, m.m10)
        .putFloat(20, m.m11)
        .putFloat(24, m.m12)
        .putFloat(28, m.m13)
        .putFloat(32, m.m20)
        .putFloat(36, m.m21)
        .putFloat(40, m.m22)
        .putFloat(44, m.m23)
        .putFloat(48, m.m30)
        .putFloat(52, m.m31)
        .putFloat(56, m.m32)
        .putFloat(60, m.m33)
}

private fun putN(m: Matrix4f, offset: Int, dest: ByteBuffer) {
    dest.putFloat(offset, m.m00)
        .putFloat(offset + 4, m.m01)
        .putFloat(offset + 8, m.m02)
        .putFloat(offset + 12, m.m03)
        .putFloat(offset + 16, m.m10)
        .putFloat(offset + 20, m.m11)
        .putFloat(offset + 24, m.m12)
        .putFloat(offset + 28, m.m13)
        .putFloat(offset + 32, m.m20)
        .putFloat(offset + 36, m.m21)
        .putFloat(offset + 40, m.m22)
        .putFloat(offset + 44, m.m23)
        .putFloat(offset + 48, m.m30)
        .putFloat(offset + 52, m.m31)
        .putFloat(offset + 56, m.m32)
        .putFloat(offset + 60, m.m33)
}

fun Matrix4f.writeTo(pos: Int, buffer: FloatBuffer) {
    if (pos == 0) {
        put0(this, buffer)
    } else {
        putN(this, pos, buffer)
    }
}

fun put0(m: Matrix4f, dest: FloatBuffer) {
    dest.put(0, m.m00)
        .put(1, m.m01)
        .put(2, m.m02)
        .put(3, m.m03)
        .put(4, m.m10)
        .put(5, m.m11)
        .put(6, m.m12)
        .put(7, m.m13)
        .put(8, m.m20)
        .put(9, m.m21)
        .put(10, m.m22)
        .put(11, m.m23)
        .put(12, m.m30)
        .put(13, m.m31)
        .put(14, m.m32)
        .put(15, m.m33)
}

fun putN(m: Matrix4f, offset: Int, dest: FloatBuffer) {
    dest.put(offset, m.m00)
        .put(offset + 1, m.m01)
        .put(offset + 2, m.m02)
        .put(offset + 3, m.m03)
        .put(offset + 4, m.m10)
        .put(offset + 5, m.m11)
        .put(offset + 6, m.m12)
        .put(offset + 7, m.m13)
        .put(offset + 8, m.m20)
        .put(offset + 9, m.m21)
        .put(offset + 10, m.m22)
        .put(offset + 11, m.m23)
        .put(offset + 12, m.m30)
        .put(offset + 13, m.m31)
        .put(offset + 14, m.m32)
        .put(offset + 15, m.m33)
}