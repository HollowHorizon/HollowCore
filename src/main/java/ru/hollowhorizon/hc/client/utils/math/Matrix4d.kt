package ru.hollowhorizon.hc.client.utils.math

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import net.minecraft.util.Mth
import java.nio.FloatBuffer
import kotlin.math.sqrt


class Matrix4d(
    var m00: Double = 1.0,
    var m01: Double = 0.0,
    var m02: Double = 0.0,
    var m03: Double = 0.0,

    var m10: Double = 0.0,
    var m11: Double = 1.0,
    var m12: Double = 0.0,
    var m13: Double = 0.0,

    var m20: Double = 0.0,
    var m21: Double = 0.0,
    var m22: Double = 1.0,
    var m23: Double = 0.0,

    var m30: Double = 0.0,
    var m31: Double = 0.0,
    var m32: Double = 0.0,
    var m33: Double = 1.0,
) {
    var translation
        get() = Vector3d(m30, m31, m32)
        set(value) {
            if (properties and PROPERTY_IDENTITY == 0) this.setIdentity()
            m30 = value.x
            m31 = value.y
            m32 = value.z
            m33 = 1.0
            properties = PROPERTY_AFFINE or PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL
        }
    val rotation
        get(): Quaternion {
            var nm00 = m00
            var nm01 = m01
            var nm02 = m02
            var nm10 = m10
            var nm11 = m11
            var nm12 = m12
            var nm20 = m20
            var nm21 = m21
            var nm22 = m22
            val lenX = 1.0 / sqrt(nm00 * nm00 + nm01 * nm01 + nm02 * nm02)
            val lenY = 1.0 / sqrt(nm10 * nm10 + nm11 * nm11 + nm12 * nm12)
            val lenZ = 1.0 / sqrt(nm20 * nm20 + nm21 * nm21 + nm22 * nm22)
            nm00 *= lenX
            nm01 *= lenX
            nm02 *= lenX
            nm10 *= lenY
            nm11 *= lenY
            nm12 *= lenY
            nm20 *= lenZ
            nm21 *= lenZ
            nm22 *= lenZ

            val tr = nm00 + nm11 + nm22

            if (tr >= 0.0) {
                var t = sqrt(tr + 1.0)
                val w = (t * 0.5).toFloat()
                t = 0.5 / t
                return Quaternion(
                    ((nm12 - nm21) * t).toFloat(),
                    ((nm20 - nm02) * t).toFloat(),
                    ((nm01 - nm10) * t).toFloat(),
                    w
                )
            } else {
                if (m00 > m11 && m00 > m22) {
                    var t = sqrt(1.0 + nm00 - nm11 - nm22)
                    val x = (t * 0.5).toFloat()
                    t = 0.5 / t
                    return Quaternion(
                        x,
                        ((nm01 + nm10) * t).toFloat(),
                        ((nm02 + nm20) * t).toFloat(),
                        ((nm12 - nm21) * t).toFloat()
                    )
                } else if (m11 > m22) {
                    var t = sqrt(1.0 + nm11 - nm00 - nm22)
                    val y = (t * 0.5).toFloat()
                    t = 0.5 / t
                    return Quaternion(
                        ((nm01 + nm10) * t).toFloat(),
                        y,
                        ((nm12 + nm21) * t).toFloat(),
                        ((nm20 - nm02) * t).toFloat()
                    )
                } else {
                    var t = sqrt(1.0 + nm22 - nm00 - nm11)
                    val z = (t * 0.5).toFloat()
                    t = 0.5 / t
                    return Quaternion(
                        ((nm02 + nm20) * t).toFloat(),
                        ((nm12 + nm21) * t).toFloat(),
                        z,
                        ((nm01 - nm10) * t).toFloat()
                    )
                }
            }
        }
    val scale
        get(): Vector3f {
            val x = sqrt(m00 * m00 + m01 * m01 + m02 * m02)
            val y = sqrt(m10 * m10 + m11 * m11 + m12 * m12)
            val z = sqrt(m20 * m20 + m21 * m21 + m22 * m22)
            return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
        }

    constructor(m: Matrix4d) : this(
        m.m00, m.m01, m.m02, m.m03,
        m.m10, m.m11, m.m12, m.m13,
        m.m20, m.m21, m.m22, m.m23,
        m.m30, m.m31, m.m32, m.m33
    )

    companion object {
        const val HALF_PI_F = (Math.PI / 2.0).toFloat()


        fun absEqualsOne(r: Double): Boolean {
            return java.lang.Double.doubleToRawLongBits(r) and 0x7FFFFFFFFFFFFFFFL == 0x3FF0000000000000L
        }

        const val PROPERTY_PERSPECTIVE = (1 shl 0)
        const val PROPERTY_AFFINE = (1 shl 1)
        const val PROPERTY_IDENTITY = (1 shl 2)
        const val PROPERTY_TRANSLATION = (1 shl 3)
        const val PROPERTY_ORTHONORMAL = (1 shl 4)
    }

    var properties = 0

    fun determineProperties() {
        var properties = 0
        if (m03 == 0.0 && m13 == 0.0) {
            if (m23 == 0.0 && m33 == 1.0) {
                properties = properties or PROPERTY_AFFINE
                if (m00 == 1.0 && m01 == 0.0 && m02 == 0.0 && m10 == 0.0 && m11 == 1.0 && m12 == 0.0 && m20 == 0.0
                    && m21 == 0.0 && m22 == 1.0
                ) {
                    properties = properties or (PROPERTY_TRANSLATION or PROPERTY_ORTHONORMAL)
                    if (m30 == 0.0 && m31 == 0.0 && m32 == 0.0) properties = properties or PROPERTY_IDENTITY
                }
            } else if (m01 == 0.0 && m02 == 0.0 && m10 == 0.0 && m12 == 0.0 && m20 == 0.0 && m21 == 0.0 && m30 == 0.0
                && m31 == 0.0 && m33 == 0.0
            ) {
                properties = properties or PROPERTY_PERSPECTIVE
            }
        }
        this.properties = properties
    }

    fun setIdentity() {
        m00 = 1.0
        m01 = 0.0
        m02 = 0.0
        m03 = 0.0
        m10 = 0.0
        m11 = 1.0
        m12 = 0.0
        m13 = 0.0
        m20 = 0.0
        m21 = 0.0
        m22 = 1.0
        m23 = 0.0
        m30 = 0.0
        m31 = 0.0
        m32 = 0.0
        m33 = 1.0
    }

    fun set(mat: Matrix4d) {
        m00 = mat.m00
        m01 = mat.m01
        m02 = mat.m02
        m03 = mat.m03
        m10 = mat.m10
        m11 = mat.m11
        m12 = mat.m12
        m13 = mat.m13
        m20 = mat.m20
        m21 = mat.m21
        m22 = mat.m22
        m23 = mat.m23
        m30 = mat.m30
        m31 = mat.m31
        m32 = mat.m32
        m33 = mat.m33
        determineProperties()
    }

    fun lerp(other: Matrix4d, t: Double): Matrix4d {
        return lerp(other, t, this)
    }

    fun lerp(other: Matrix4d, t: Double, dest: Matrix4d): Matrix4d {
        dest.m00 = m00 + (other.m00 - m00) * t
        dest.m01 = m01 + (other.m01 - m01) * t
        dest.m02 = m02 + (other.m02 - m02) * t
        dest.m03 = m03 + (other.m03 - m03) * t
        dest.m10 = m10 + (other.m10 - m10) * t
        dest.m11 = m11 + (other.m11 - m11) * t
        dest.m12 = m12 + (other.m12 - m12) * t
        dest.m13 = m13 + (other.m13 - m13) * t
        dest.m20 = m20 + (other.m20 - m20) * t
        dest.m21 = m21 + (other.m21 - m21) * t
        dest.m22 = m22 + (other.m22 - m22) * t
        dest.m23 = m23 + (other.m23 - m23) * t
        dest.m30 = m30 + (other.m30 - m30) * t
        dest.m31 = m31 + (other.m31 - m31) * t
        dest.m32 = m32 + (other.m32 - m32) * t
        dest.m33 = m33 + (other.m33 - m33) * t
        return dest
    }

    fun rotateAffineZYX(angleZ: Double, angleY: Double, angleX: Double, dest: Matrix4d): Matrix4d {
        val sinX = Mth.sin(angleX.toFloat()).toDouble()
        val cosX: Double = Mth.sin(angleX.toFloat() + HALF_PI_F).toDouble()
        val sinY = Mth.sin(angleY.toFloat()).toDouble()
        val cosY: Double = Mth.sin(angleY.toFloat() + HALF_PI_F).toDouble()
        val sinZ = Mth.sin(angleZ.toFloat()).toDouble()
        val cosZ: Double = Mth.sin(angleZ.toFloat() + HALF_PI_F).toDouble()
        val m_sinZ = -sinZ
        val m_sinY = -sinY
        val m_sinX = -sinX

        // rotateZ
        val nm00 = m00 * cosZ + m10 * sinZ
        val nm01 = m01 * cosZ + m11 * sinZ
        val nm02 = m02 * cosZ + m12 * sinZ
        val nm10 = m00 * m_sinZ + m10 * cosZ
        val nm11 = m01 * m_sinZ + m11 * cosZ
        val nm12 = m02 * m_sinZ + m12 * cosZ
        // rotateY
        val nm20 = nm00 * sinY + m20 * cosY
        val nm21 = nm01 * sinY + m21 * cosY
        val nm22 = nm02 * sinY + m22 * cosY
        dest.m00 = nm00 * cosY + m20 * m_sinY
        dest.m01 = nm01 * cosY + m21 * m_sinY
        dest.m02 = nm02 * cosY + m22 * m_sinY
        dest.m03 = 0.0 // rotateX
        dest.m10 = nm10 * cosX + nm20 * sinX
        dest.m11 = nm11 * cosX + nm21 * sinX
        dest.m12 = nm12 * cosX + nm22 * sinX
        dest.m13 = 0.0
        dest.m20 = nm10 * m_sinX + nm20 * cosX
        dest.m21 = nm11 * m_sinX + nm21 * cosX
        dest.m22 = nm12 * m_sinX + nm22 * cosX
        dest.m23 = 0.0
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = 1.0
        properties = properties and (PROPERTY_PERSPECTIVE or PROPERTY_IDENTITY or PROPERTY_TRANSLATION).inv()
        return dest
    }

    fun rotateAffineZYX(angleZ: Double, angleY: Double, angleX: Double): Matrix4d {
        return rotateAffineZYX(angleZ, angleY, angleX, this)
    }

    fun getEulerAnglesZYX(): Vector3d {
        return Vector3d(
            Mth.atan2(m12, m22),
            Mth.atan2(-m02, sqrt(1.0 - m02 * m02)),
            Mth.atan2(m01, m00)
        )
    }

    fun scale(v: Vector3d): Matrix4d {
        return scaleGeneric(v.x, v.y, v.z)
    }

    private fun scaleGeneric(x: Double, y: Double, z: Double): Matrix4d {
        if (properties and PROPERTY_IDENTITY == 0) setIdentity()
        val one = absEqualsOne(x) && absEqualsOne(y) && absEqualsOne(z)
        m00 = x
        m11 = y
        m22 = z
        properties = PROPERTY_AFFINE or if (one) PROPERTY_ORTHONORMAL else 0
        return this
    }

    fun mulLocalAffine(left: Matrix4d): Matrix4d {
        return mulLocalAffine(left, this)
    }

    fun mulLocalAffine(left: Matrix4d, dest: Matrix4d): Matrix4d {
        val nm00: Double = left.m00 * m00 + left.m10 * m01 + left.m20 * m02
        val nm01: Double = left.m01 * m00 + left.m11 * m01 + left.m21 * m02
        val nm02: Double = left.m02 * m00 + left.m12 * m01 + left.m22 * m02
        val nm03: Double = left.m03
        val nm10: Double = left.m00 * m10 + left.m10 * m11 + left.m20 * m12
        val nm11: Double = left.m01 * m10 + left.m11 * m11 + left.m21 * m12
        val nm12: Double = left.m02 * m10 + left.m12 * m11 + left.m22 * m12
        val nm13: Double = left.m13
        val nm20: Double = left.m00 * m20 + left.m10 * m21 + left.m20 * m22
        val nm21: Double = left.m01 * m20 + left.m11 * m21 + left.m21 * m22
        val nm22: Double = left.m02 * m20 + left.m12 * m21 + left.m22 * m22
        val nm23: Double = left.m23
        val nm30: Double = left.m00 * m30 + left.m10 * m31 + left.m20 * m32 + left.m30
        val nm31: Double = left.m01 * m30 + left.m11 * m31 + left.m21 * m32 + left.m31
        val nm32: Double = left.m02 * m30 + left.m12 * m31 + left.m22 * m32 + left.m32
        val nm33: Double = left.m33
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m10 = nm10
        dest.m11 = nm11
        dest.m12 = nm12
        dest.m13 = nm13
        dest.m20 = nm20
        dest.m21 = nm21
        dest.m22 = nm22
        dest.m23 = nm23
        dest.m30 = nm30
        dest.m31 = nm31
        dest.m32 = nm32
        dest.m33 = nm33
        properties = PROPERTY_AFFINE
        return dest
    }

    fun invertAffine(dest: Matrix4d): Matrix4d {
        val m11m00 = m00 * m11
        val m10m01 = m01 * m10
        val m10m02 = m02 * m10
        val m12m00 = m00 * m12
        val m12m01 = m01 * m12
        val m11m02 = m02 * m11
        val s = 1.0 / ((m11m00 - m10m01) * m22 + (m10m02 - m12m00) * m21 + (m12m01 - m11m02) * m20)
        val m10m22 = m10 * m22
        val m10m21 = m10 * m21
        val m11m22 = m11 * m22
        val m11m20 = m11 * m20
        val m12m21 = m12 * m21
        val m12m20 = m12 * m20
        val m20m02 = m20 * m02
        val m20m01 = m20 * m01
        val m21m02 = m21 * m02
        val m21m00 = m21 * m00
        val m22m01 = m22 * m01
        val m22m00 = m22 * m00
        val nm00 = (m11m22 - m12m21) * s
        val nm01 = (m21m02 - m22m01) * s
        val nm02 = (m12m01 - m11m02) * s
        val nm10 = (m12m20 - m10m22) * s
        val nm11 = (m22m00 - m20m02) * s
        val nm12 = (m10m02 - m12m00) * s
        val nm20 = (m10m21 - m11m20) * s
        val nm21 = (m20m01 - m21m00) * s
        val nm22 = (m11m00 - m10m01) * s
        val nm30 = (m10m22 * m31 - m10m21 * m32 + m11m20 * m32 - m11m22 * m30 + m12m21 * m30 - m12m20 * m31) * s
        val nm31 = (m20m02 * m31 - m20m01 * m32 + m21m00 * m32 - m21m02 * m30 + m22m01 * m30 - m22m00 * m31) * s
        val nm32 = (m11m02 * m30 - m12m01 * m30 + m12m00 * m31 - m10m02 * m31 + m10m01 * m32 - m11m00 * m32) * s
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = 0.0
        dest.m10 = nm10
        dest.m11 = nm11
        dest.m12 = nm12
        dest.m13 = 0.0
        dest.m20 = nm20
        dest.m21 = nm21
        dest.m22 = nm22
        dest.m23 = 0.0
        dest.m30 = nm30
        dest.m31 = nm31
        dest.m32 = nm32
        dest.m33 = 1.0
        dest.properties = PROPERTY_AFFINE
        return dest
    }

    fun invertAffine(): Matrix4d {
        return invertAffine(this)
    }

    fun mulAffine(right: Matrix4d, dest: Matrix4d): Matrix4d {
        val m00 = m00
        val m01 = m01
        val m02 = m02
        val m10 = m10
        val m11 = m11
        val m12 = m12
        val m20 = m20
        val m21 = m21
        val m22 = m22
        val rm00: Double = right.m00
        val rm01: Double = right.m01
        val rm02: Double = right.m02
        val rm10: Double = right.m10
        val rm11: Double = right.m11
        val rm12: Double = right.m12
        val rm20: Double = right.m20
        val rm21: Double = right.m21
        val rm22: Double = right.m22
        val rm30: Double = right.m30
        val rm31: Double = right.m31
        val rm32: Double = right.m32
        dest.m00 = m00 * rm00 + (m10 * rm01 + m20 * rm02)
        dest.m01 = m01 * rm00 + (m11 * rm01 + m21 * rm02)
        dest.m02 = m02 * rm00 + (m12 * rm01 + m22 * rm02)
        dest.m03 = m03
        dest.m10 = m00 * rm10 + (m10 * rm11 + m20 * rm12)
        dest.m11 = m01 * rm10 + (m11 * rm11 + m21 * rm12)
        dest.m12 = m02 * rm10 + (m12 * rm11 + m22 * rm12)
        dest.m13 = m13
        dest.m20 = m00 * rm20 + (m10 * rm21 + m20 * rm22)
        dest.m21 = m01 * rm20 + (m11 * rm21 + m21 * rm22)
        dest.m22 = m02 * rm20 + (m12 * rm21 + m22 * rm22)
        dest.m23 = m23
        dest.m30 = m00 * rm30 + (m10 * rm31 + m20 * rm32) + m30
        dest.m31 = m01 * rm30 + (m11 * rm31 + m21 * rm32) + m31
        dest.m32 = m02 * rm30 + (m12 * rm31 + m22 * rm32) + m32
        dest.m33 = m33
        dest.properties = PROPERTY_AFFINE or (properties and right.properties and PROPERTY_ORTHONORMAL)
        return dest
    }

    fun mulAffine(right: Matrix4d): Matrix4d {
        return mulAffine(right, this)
    }

    fun get(offset: Int, uniformFloatBuffer: FloatBuffer) {
        uniformFloatBuffer.put(offset, m00.toFloat())
        uniformFloatBuffer.put(offset + 1, m01.toFloat())
        uniformFloatBuffer.put(offset + 2, m02.toFloat())
        uniformFloatBuffer.put(offset + 3, m03.toFloat())
        uniformFloatBuffer.put(offset + 4, m10.toFloat())
        uniformFloatBuffer.put(offset + 5, m11.toFloat())
        uniformFloatBuffer.put(offset + 6, m12.toFloat())
        uniformFloatBuffer.put(offset + 7, m13.toFloat())
        uniformFloatBuffer.put(offset + 8, m20.toFloat())
        uniformFloatBuffer.put(offset + 9, m21.toFloat())
        uniformFloatBuffer.put(offset + 10, m22.toFloat())
        uniformFloatBuffer.put(offset + 11, m23.toFloat())
        uniformFloatBuffer.put(offset + 12, m30.toFloat())
        uniformFloatBuffer.put(offset + 13, m31.toFloat())
        uniformFloatBuffer.put(offset + 14, m32.toFloat())
        uniformFloatBuffer.put(offset + 15, m33.toFloat())
    }

    override fun toString(): String {
        return """
            Matrix4d:
            $m00, $m01, $m02, $m03
            $m10, $m11, $m12, $m13
            $m20, $m21, $m22, $m23
            $m30, $m31, $m32, $m33
        """.trimIndent()
    }

    fun toMC(): Matrix4f {
        return Matrix4f(
            floatArrayOf(
                m00.toFloat(), m01.toFloat(), m02.toFloat(), m03.toFloat(),
                m10.toFloat(), m11.toFloat(), m12.toFloat(), m13.toFloat(),
                m20.toFloat(), m21.toFloat(), m22.toFloat(), m23.toFloat(),
                m30.toFloat(), m31.toFloat(), m32.toFloat(), m33.toFloat()
            )
        )
    }
}