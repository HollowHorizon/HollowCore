/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

class LinearSingle(keys: FloatArray, values: Array<FloatArray>) : Interpolator<FloatArray>(keys, values) {
    override fun compute(time: Float): FloatArray {
        if (time <= keys.first() || keys.size == 1) return values.first()
        else if (time >= keys.last()) return values.last()
        else {
            val previousIndex = time.animIndex
            val nextIndex = previousIndex + 1
            val local = time - keys[previousIndex]
            val delta = keys[nextIndex] - keys[previousIndex]
            val alpha = local / delta
            val previousValue = values[previousIndex]
            val nextValue = values[nextIndex]
            val interpolatedValue = FloatArray(previousValue.size) { i -> previousValue[i] + (nextValue[i] - previousValue[i]) * alpha }
            return interpolatedValue
        }
    }

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