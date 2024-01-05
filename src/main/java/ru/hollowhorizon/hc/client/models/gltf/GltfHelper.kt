package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import ru.hollowhorizon.hc.HollowCore
import kotlin.math.abs
import kotlin.math.sqrt

fun Matrix4f.invertMatrix() {
    val m0 = m00
    val m1 = m01
    val m2 = m02
    val m3 = m03
    val m4 = m10
    val m5 = m11
    val m6 = m12
    val m7 = m13
    val m8 = m20
    val m9 = m21
    val mA = m22
    val mB = m23
    val mC = m30
    val mD = m31
    val mE = m32
    val mF = m33

    m00 = m5 * mA * mF - m5 * mB * mE - m9 * m6 * mF + m9 * m7 * mE + mD * m6 * mB - mD * m7 * mA
    m10 = -m4 * mA * mF + m4 * mB * mE + m8 * m6 * mF - m8 * m7 * mE - mC * m6 * mB + mC * m7 * mA
    m20 = m4 * m9 * mF - m4 * mB * mD - m8 * m5 * mF + m8 * m7 * mD + mC * m5 * mB - mC * m7 * m9
    m30 = -m4 * m9 * mE + m4 * mA * mD + m8 * m5 * mE - m8 * m6 * mD - mC * m5 * mA + mC * m6 * m9
    m01 = -m1 * mA * mF + m1 * mB * mE + m9 * m2 * mF - m9 * m3 * mE - mD * m2 * mB + mD * m3 * mA
    m11 = m0 * mA * mF - m0 * mB * mE - m8 * m2 * mF + m8 * m3 * mE + mC * m2 * mB - mC * m3 * mA
    m21 = -m0 * m9 * mF + m0 * mB * mD + m8 * m1 * mF - m8 * m3 * mD - mC * m1 * mB + mC * m3 * m9
    m31 = m0 * m9 * mE - m0 * mA * mD - m8 * m1 * mE + m8 * m2 * mD + mC * m1 * mA - mC * m2 * m9
    m02 = m1 * m6 * mF - m1 * m7 * mE - m5 * m2 * mF + m5 * m3 * mE + mD * m2 * m7 - mD * m3 * m6
    m12 = -m0 * m6 * mF + m0 * m7 * mE + m4 * m2 * mF - m4 * m3 * mE - mC * m2 * m7 + mC * m3 * m6
    m22 = m0 * m5 * mF - m0 * m7 * mD - m4 * m1 * mF + m4 * m3 * mD + mC * m1 * m7 - mC * m3 * m5
    m32 = -m0 * m5 * mE + m0 * m6 * mD + m4 * m1 * mE - m4 * m2 * mD - mC * m1 * m6 + mC * m2 * m5
    m03 = -m1 * m6 * mB + m1 * m7 * mA + m5 * m2 * mB - m5 * m3 * mA - m9 * m2 * m7 + m9 * m3 * m6
    m13 = m0 * m6 * mB - m0 * m7 * mA - m4 * m2 * mB + m4 * m3 * mA + m8 * m2 * m7 - m8 * m3 * m6
    m23 = -m0 * m5 * mB + m0 * m7 * m9 + m4 * m1 * mB - m4 * m3 * m9 - m8 * m1 * m7 + m8 * m3 * m5
    m33 = m0 * m5 * mA - m0 * m6 * m9 - m4 * m1 * mA + m4 * m2 * m9 + m8 * m1 * m6 - m8 * m2 * m5

    val det = m0 * m00 + m1 * m10 + m2 * m20 + m3 * m30
    if (abs(det.toDouble()) <= 1E-8f) {
        HollowCore.LOGGER.error("Matrix is not invertible, determinant is $det, returning identity")
        setIdentity()
        return
    }
    val invDet = 1.0f / det
    m00 *= invDet
    m01 *= invDet
    m02 *= invDet
    m03 *= invDet
    m10 *= invDet
    m11 *= invDet
    m12 *= invDet
    m13 *= invDet
    m20 *= invDet
    m21 *= invDet
    m22 *= invDet
    m23 *= invDet
    m30 *= invDet
    m31 *= invDet
    m32 *= invDet
    m33 *= invDet
}

fun Quaternion.store(m: Matrix4f) {
    val invLength = 1.0f / sqrt(i() * i() + j() * j() + k() * k() + r() * r())
    // Adapted from javax.vecmath.Matrix4f
    val qx = this.i() * invLength
    val qy = this.j() * invLength
    val qz = this.k() * invLength
    val qw = this.r() * invLength
    m.m00 = 1.0f - 2.0f * qy * qy - 2.0f * qz * qz
    m.m01 = 2.0f * (qx * qy + qw * qz)
    m.m02 = 2.0f * (qx * qz - qw * qy)
    m.m03 = 0.0f
    m.m10 = 2.0f * (qx * qy - qw * qz)
    m.m11 = 1.0f - 2.0f * qx * qx - 2.0f * qz * qz
    m.m12 = 2.0f * (qy * qz + qw * qx)
    m.m13 = 0.0f
    m.m20 = 2.0f * (qx * qz + qw * qy)
    m.m21 = 2.0f * (qy * qz - qw * qx)
    m.m22 = 1.0f - 2.0f * qx * qx - 2.0f * qy * qy
    m.m23 = 0.0f
    m.m30 = 0.0f
    m.m31 = 0.0f
    m.m32 = 0.0f
    m.m33 = 1.0f
}

private fun dot(a: FloatArray, b: FloatArray): Float {
    var sum = 0f
    for (i in a.indices) {
        sum += a[i] * b[i]
    }
    return sum
}