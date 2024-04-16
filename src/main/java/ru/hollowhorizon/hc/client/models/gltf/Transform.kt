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

package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix3f
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import kotlinx.serialization.Serializable

@Serializable
data class Transform(
    val tX: Float = 0f, val tY: Float = 0f, val tZ: Float = 0f,
    val rX: Float = 0f, val rY: Float = 0f, val rZ: Float = 0f,
    val sX: Float = 1.0f, val sY: Float = 1.0f, val sZ: Float = 1.0f,
) {
    val matrix: Matrix4f
        get() {
            val matrix = Matrix4f()
            matrix.setIdentity()
            matrix.translate(Vector3f(tX, tY, tZ))
            matrix.multiply(Vector3f.XP.rotationDegrees(rX))
            matrix.multiply(Vector3f.YP.rotationDegrees(rY))
            matrix.multiply(Vector3f.ZP.rotationDegrees(rZ))
            matrix.multiply(Matrix4f.createScaleMatrix(sX, sY, sZ))
            return matrix
        }
    val normalMatrix: Matrix3f
        get() {
            val matrix = Matrix3f()
            matrix.setIdentity()
            matrix.mul(Vector3f.XP.rotationDegrees(rX))
            matrix.mul(Vector3f.YP.rotationDegrees(rY))
            matrix.mul(Vector3f.ZP.rotationDegrees(rZ))
            matrix.mul(Matrix3f.createScaleMatrix(sX, sY, sZ))
            return matrix
        }

    companion object {
        fun create(builder: Builder.() -> Unit) = Builder().apply(builder).build()

        class Builder {
            var tX: Float = 0f
            var tY: Float = 0f
            var tZ: Float = 0f
            var rX: Float = 0f
            var rY: Float = 0f
            var rZ: Float = 0f
            var sX: Float = 1.0f
            var sY: Float = 1.0f
            var sZ: Float = 1.0f

            fun translate(x: Float, y: Float, z: Float) {
                tX = x
                tY = y
                tZ = z
            }

            fun scale(x: Float, y: Float, z: Float) {
                sX = x
                sY = y
                sZ = z
            }

            fun rotate(x: Float, y: Float, z: Float) {
                rX = x
                rY = y
                rZ = z
            }

            fun build() = Transform(tX, tY, tZ, rX, rY, rZ, sX, sY, sZ)
        }
    }
}