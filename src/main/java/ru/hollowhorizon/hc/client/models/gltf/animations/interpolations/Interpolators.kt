package ru.hollowhorizon.hc.client.models.gltf.animations.interpolations

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

class Vec3Step(keys: FloatArray, values: Array<Vector3f>) : Interpolator<Vector3f>(keys, values) {
    override fun compute(time: Float): Vector3f = values[time.animIndex]
}

class QuatStep(keys: FloatArray, values: Array<Quaternion>) : Interpolator<Quaternion>(keys, values) {
    override fun compute(time: Float): Quaternion = values[time.animIndex]
}

class Linear(keys: FloatArray, values: Array<Vector3f>) : Interpolator<Vector3f>(keys, values) {
    override fun compute(time: Float): Vector3f {
        if (time <= keys.first() || keys.size == 1) return values.first()
        else if (time >= keys.last()) return values.last()
        else {
            val previousIndex = time.animIndex
            val nextIndex = previousIndex + 1
            val local = time - keys[previousIndex]
            val delta = keys[nextIndex] - keys[previousIndex]
            val alpha = local / delta
            val previousPoint = values[previousIndex].copy()
            val nextPoint = values[nextIndex]
            previousPoint.lerp(nextPoint, alpha)
            return previousPoint
        }
    }
}

class SphericalLinear(keys: FloatArray, values: Array<Quaternion>) : Interpolator<Quaternion>(keys, values) {
    override fun compute(time: Float): Quaternion {
        if (time <= keys.first() || keys.size == 1) return values.first()
        else if (time >= keys.last()) return values.last()
        else {
            val previousIndex = time.animIndex
            val nextIndex = previousIndex + 1

            val local = time - keys[previousIndex]
            val delta = keys[nextIndex] - keys[previousIndex]
            val alpha = local / delta

            val previousPoint = values[previousIndex].copy()
            val nextPoint = values[nextIndex]
            previousPoint.sphericalLerp(nextPoint, alpha)
            return previousPoint
        }
    }

}

fun Quaternion.sphericalLerp(target: Quaternion, alpha: Float) {
    val cosom = Math.fma(i(), target.i(), Math.fma(j(), target.j(), Math.fma(k(), target.k(), r() * target.r())))
    val absCosom = abs(cosom)
    val scale0: Float
    var scale1: Float
    if (1.0f - absCosom > 1E-6f) {
        val sinSqr = 1.0f - absCosom * absCosom
        val sinom = 1.0f / sqrt(sinSqr)
        val omega = atan2(sinSqr * sinom, absCosom)
        scale0 = (sin((1.0f - alpha) * omega) * sinom)
        scale1 = (sin(alpha * omega) * sinom)
    } else {
        scale0 = 1.0f - alpha
        scale1 = alpha
    }
    scale1 = if (cosom >= 0.0f) scale1 else -scale1
    set(
        Math.fma(scale0, i(), scale1 * target.i()),
        Math.fma(scale0, j(), scale1 * target.j()),
        Math.fma(scale0, k(), scale1 * target.k()),
        Math.fma(scale0, r(), scale1 * target.r())
    )
}