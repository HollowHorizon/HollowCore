package ru.hollowhorizon.hc.client.utils.math

import dev.folomeev.kotgl.matrix.matrices.Mat3
import dev.folomeev.kotgl.matrix.matrices.Mat4
import dev.folomeev.kotgl.matrix.vectors.Vec3
import dev.folomeev.kotgl.matrix.vectors.Vec4
import dev.folomeev.kotgl.matrix.vectors.mutables.MutableVec3
import dev.folomeev.kotgl.matrix.vectors.mutables.mutableVec3
import dev.folomeev.kotgl.matrix.vectors.mutables.set
import dev.folomeev.kotgl.matrix.vectors.vec3
import dev.folomeev.kotgl.matrix.vectors.vec4

fun Float.lerp(other: Float, alpha: Float) = this + (other - this) * alpha

fun catmullRom(
    t: Float,
    a: Float,
    b: Float,
    c: Float,
    d: Float,
): Float {
    val v0 = -0.5f * a + 1.5f * b - 1.5f * c + 0.5f * d
    val v1 = a - 2.5f * b + 2 * c - 0.5f * d
    val v2 = -0.5f * a + 0.5f * c
    val tt = t * t
    return v0 * t * tt + v1 * tt + v2 * t + b
}

fun bezier(
    t: Float,
    a: Float,
    b: Float,
    c: Float,
    d: Float,
): Float {
    val ab = a.lerp(b, t)
    val bc = b.lerp(c, t)
    val cd = c.lerp(d, t)
    val abc = ab.lerp(bc, t)
    val bcd = bc.lerp(cd, t)
    return abc.lerp(bcd, t)
}

inline fun <T> Vec3.times(mat: Mat3, out: (Float, Float, Float) -> T) = out(
    x * mat.m00 + y * mat.m01 + z * mat.m02,
    x * mat.m10 + y * mat.m11 + z * mat.m12,
    x * mat.m20 + y * mat.m21 + z * mat.m22,
)

inline fun <T> Vec4.times(mat: Mat4, out: (Float, Float, Float, Float) -> T) = out(
    x * mat.m00 + y * mat.m01 + z * mat.m02 + w * mat.m03,
    x * mat.m10 + y * mat.m11 + z * mat.m12 + w * mat.m13,
    x * mat.m20 + y * mat.m21 + z * mat.m22 + w * mat.m23,
    x * mat.m30 + y * mat.m31 + z * mat.m32 + w * mat.m33,
)

fun Vec3.times(mat: Mat3) = times(mat, ::vec3)
fun Vec4.times(mat: Mat4) = times(mat, ::vec4)

inline fun <T> Vec3.rotateBy(q: Quaternion, out: (Float, Float, Float) -> T): T =
    with(q * Quaternion(x, y, z, 0f) * q.conjugate()) { out(x, y, z) }

fun Vec3.rotateBy(q: Quaternion) = rotateBy(q, ::mutableVec3)
fun MutableVec3.rotateSelfBy(q: Quaternion) = rotateBy(q, ::set)

