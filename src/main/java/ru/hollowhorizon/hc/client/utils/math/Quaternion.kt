package ru.hollowhorizon.hc.client.utils.math

import dev.folomeev.kotgl.matrix.matrices.Mat3
import dev.folomeev.kotgl.matrix.matrices.mat3
import dev.folomeev.kotgl.matrix.vectors.Vec3
import dev.folomeev.kotgl.matrix.vectors.dot
import dev.folomeev.kotgl.matrix.vectors.mutables.cross
import dev.folomeev.kotgl.matrix.vectors.mutables.minus
import dev.folomeev.kotgl.matrix.vectors.mutables.mutableVec3
import dev.folomeev.kotgl.matrix.vectors.mutables.normalizeSelf
import dev.folomeev.kotgl.matrix.vectors.mutables.times
import dev.folomeev.kotgl.matrix.vectors.vecZero
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {
    fun normalize(): Quaternion {
        val invNorm = 1 / (x * x + y * y + z * z + w * w)
        return Quaternion(x * invNorm, y * invNorm, z * invNorm, w * invNorm)
    }

    fun conjugate(): Quaternion =
        Quaternion(-x, -y, -z, w)

    fun invert(): Quaternion {
        val invNorm = 1 / (x * x + y * y + z * z + w * w)
        return Quaternion(-x * invNorm, -y * invNorm, -z * invNorm, w * invNorm)
    }

    operator fun times(q: Quaternion): Quaternion {
        if (this === Identity) return q
        if (q === Identity) return this
        return Quaternion(
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w,
            w * q.w - x * q.x - y * q.y - z * q.z,
        )
    }

    operator fun times(v: Vec3): Vec3 = v.rotateBy(this)

    fun projectAroundAxis(axis: Vec3): Quaternion {
        val rotationAxis = mutableVec3(x, y, z)
        val projectedLength = axis.dot(rotationAxis)
        val projectedAxis = axis.times(projectedLength)
        return if (projectedLength > 0) {
            Quaternion(projectedAxis.x, projectedAxis.y, projectedAxis.z, w).normalize()
        } else {
            Quaternion(-projectedAxis.x, -projectedAxis.y, -projectedAxis.z, -w).normalize()
        }
    }

    fun opposite() = this * Y180

    companion object {
        val Identity = Quaternion(0f, 0f, 0f, 1f)

        val X180 = Quaternion(1f, 0f, 0f, 0f)
        val Y180 = Quaternion(0f, 1f, 0f, 0f)
        val Z180 = Quaternion(0f, 0f, 1f, 0f)

        fun fromAxisAngle(axis: Vec3, angleRad: Float): Quaternion {
            if (angleRad == 0f) {
                return Identity
            }
            val s = sin(angleRad * 0.5f)
            val c = cos(angleRad * 0.5f)
            return Quaternion(axis.x * s, axis.y * s, axis.z * s, c)
        }

        fun fromLookAt(lookAt: Vec3, up: Vec3): Quaternion {
            val z = vecZero().minus(lookAt).normalizeSelf()
            val x = up.cross(z).normalizeSelf()
            val y = z.cross(x)
            return fromRotationMatrix(mat3(
                x.x, y.x, z.x,
                x.y, y.y, z.y,
                x.z, y.z, z.z,
            ))
        }

        fun fromRotationMatrix(m: Mat3): Quaternion = with(m) {
            val trace = m00 + m11 + m22
            if (trace >= 0) {
                val r = sqrt(trace + 1f)
                val s = 0.5f / r
                return Quaternion(
                    (m21 - m12) * s,
                    (m02 - m20) * s,
                    (m10 - m01) * s,
                    0.5f * r,
                )
            } else {
                if (m00 >= m11 && m00 >= m22) {
                    val r = sqrt(m00 - (m11 + m22) + 1f)
                    val s = 0.5f / r
                    return Quaternion(
                        0.5f * r,
                        (m10 + m01) * s,
                        (m02 + m20) * s,
                        (m21 - m12) * s,
                    )
                } else if (m11 > m22) {
                    val r = sqrt(m11 - (m22 + m00) + 1f)
                    val s = 0.5f / r
                    return Quaternion(
                        (m10 + m01) * s,
                        0.5f * r,
                        (m21 + m12) * s,
                        (m02 - m20) * s,
                    )
                } else {
                    val r = sqrt(m22 - (m00 + m11) + 1f)
                    val s = 0.5f / r
                    return Quaternion(
                        (m02 + m20) * s,
                        (m21 + m12) * s,
                        0.5f * r,
                        (m10 - m01) * s,
                    )
                }
            }
        }
    }
}