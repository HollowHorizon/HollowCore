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

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs

@Serializable
data class Transformation(
    var translationX: Float,
    var translationY: Float,
    var translationZ: Float,
    var rotationX: Float,
    var rotationY: Float,
    var rotationZ: Float,
    var rotationW: Float,
    var scaleX: Float,
    var scaleY: Float,
    var scaleZ: Float,
    var hasTranslation: Boolean = true,
    var hasRotation: Boolean = true,
    var hasScale: Boolean = true,
    var weights: List<Float> = ArrayList(),
    @Transient
    private val matrix: Matrix4f = Matrix4f(),
) {

    val translation: Vector3f get() = Vector3f(translationX, translationY, translationZ)
    val rotation: Quaternionf get() = Quaternionf(rotationX, rotationY, rotationZ, rotationW)
    val scale: Vector3f get() = Vector3f(scaleX, scaleY, scaleZ)

    fun add(transform: Transformation, simpleRot: Boolean = false) {
        if (transform.hasTranslation) {
            translationX += transform.translationX
            translationY += transform.translationY
            translationZ += transform.translationZ
        }

        if (transform.hasRotation) {
            val res = rotation
            if (!simpleRot) res.mulLeft(transform.rotation)
            else res.mul(transform.rotation)

            rotationX = res.x()
            rotationY = res.y()
            rotationZ = res.z()
            rotationW = res.w()
        }

        if (transform.hasScale) {
            scaleX *= transform.scaleX
            scaleY *= transform.scaleY
            scaleZ *= transform.scaleZ
        }
        if (transform.weights.isNotEmpty()) {
            weights = transform.weights
        }

        hasTranslation = transform.hasTranslation
        hasRotation = transform.hasRotation
        hasScale = transform.hasScale
    }

    fun Quaternionf.mulLeft(q: Quaternionf) {
        set(
            q.w() * x() + q.x() * w() + q.y() * z() - q.z() * y(),
            q.w() * y() - q.x() * z() + q.y() * w() + q.z() * x(),
            q.w() * z() + q.x() * y() - q.y() * x() + q.z() * w(),
            q.w() * w() - q.x() * x() - q.y() * y() - q.z() * z()
        )
    }

    fun sub(transform: Transformation) {
        if (transform.hasTranslation) {
            translationX = transform.translationX - translationX
            translationY = transform.translationY - translationY
            translationZ = transform.translationZ - translationZ
        }

        if (transform.hasRotation) {
            val res = transform.rotation
            res.mul(rotation.invert())
            rotationX = res.x()
            rotationY = res.y()
            rotationZ = res.z()
            rotationW = res.w()
        }

        if (transform.hasScale) {
            scaleX = transform.scaleX / scaleX
            scaleY = transform.scaleY / scaleY
            scaleZ = transform.scaleZ / scaleZ
        }

        if (transform.weights.isNotEmpty()) {
            weights = transform.weights
        }

        hasTranslation = transform.hasTranslation
        hasRotation = transform.hasRotation
        hasScale = transform.hasScale
    }

    fun mul(factor: Float): Transformation {
        translationX *= factor
        translationY *= factor
        translationZ *= factor

        val q = Quaternionf(0f, 0f, 0f, 1f).lerp(rotation, factor)
        rotationX = q?.x() ?: rotationX
        rotationY = q?.y() ?: rotationY
        rotationZ = q?.z() ?: rotationZ
        rotationW = q?.w() ?: rotationW

        scaleX = scaleX * factor + 1f * (1f - factor)
        scaleY = scaleY * factor + 1f * (1f - factor)
        scaleZ = scaleZ * factor + 1f * (1f - factor)
        return this
    }

    fun setTranslation(array: Vector3f?) {
        if (array == null) return
        translationX = array.x()
        translationY = array.y()
        translationZ = array.z()
    }

    fun addRotation(array: Quaternionf) {

        val res = rotation
        //Рубрика: угадай сколько часов потребовалось, чтобы понять, что нужно использовать этот метод
        res.mulLeft(array)
        rotationX = res.x()
        rotationY = res.y()
        rotationZ = res.z()
        rotationW = res.w()
    }

    fun addRotationRight(array: Quaternionf) {
        val res = rotation
        res.mul(array)
        rotationX = res.x()
        rotationY = res.y()
        rotationZ = res.z()
        rotationW = res.w()
    }

    fun setRotation(array: Quaternionf?) {
        if (array == null) return
        rotationX = array.x()
        rotationY = array.y()
        rotationZ = array.z()
        rotationW = array.w()
    }

    fun setScale(array: Vector3f?) {
        if (array == null) return
        scaleX = array.x()
        scaleY = array.y()
        scaleZ = array.z()
    }

    fun set(transformation: Transformation) {
        if (transformation.hasTranslation) {
            translationX = transformation.translationX
            translationY = transformation.translationY
            translationZ = transformation.translationZ
        }
        if (transformation.hasRotation) {
            rotationX = transformation.rotationX
            rotationY = transformation.rotationY
            rotationZ = transformation.rotationZ
            rotationW = transformation.rotationW
        }
        if (transformation.hasScale) {
            scaleX = transformation.scaleX
            scaleY = transformation.scaleY
            scaleZ = transformation.scaleZ
        }
        if (transformation.weights.isNotEmpty()) {
            weights = transformation.weights
        }
    }

    @JvmOverloads
    constructor(
        translation: Vector3f? = Vector3f(),
        rotation: Quaternionf? = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
        scale: Vector3f? = Vector3f(1.0f, 1.0f, 1.0f),
        matrix: Matrix4f = Matrix4f(),
        weights: List<Float> = ArrayList(),
    ) : this(
        translation?.x() ?: 0f, translation?.y() ?: 0f, translation?.z() ?: 0f,
        rotation?.x() ?: 0f, rotation?.y() ?: 0f, rotation?.z() ?: 0f, rotation?.w() ?: 1f,
        scale?.x() ?: 1f, scale?.y() ?: 1f, scale?.z() ?: 1f,
        translation != null, rotation != null, scale != null,
        weights, matrix
    )

    fun getMatrix(): Matrix4f {
        return Matrix4f()
            .translate(translation)
            .rotate(rotation.normalize())
            .scale(scale)
            .mul(matrix)
    }

    fun getNormalMatrix(): Matrix3f {
        return Matrix3f()
            .rotate(rotation.normalize())
            .scale(scale)
    }

    fun setLocal(node: GltfTree.Node, animPose: Transformation) = set(node.fromLocal(animPose))

    override fun equals(other: Any?): Boolean {
        if (other is Transformation) {
            if (hasTranslation) {
                if (translationX != other.translationX) return false
                if (translationY != other.translationY) return false
                if (translationZ != other.translationZ) return false
            }
            if (hasRotation) {
                if (abs(rotationY - other.rotationY) > 0.1) return false
                if (abs(rotationZ - other.rotationZ) > 0.1) return false
                if (abs(rotationX - other.rotationX) > 0.1) return false
                if (abs(rotationW - other.rotationW) > 0.1) return false
            }
            if (hasScale) {
                if (scaleX != other.scaleX) return false
                if (scaleY != other.scaleY) return false
                if (scaleZ != other.scaleZ) return false
            }
            return true
        } else return super.equals(other)
    }

    companion object {

        fun lerp(first: Transformation?, second: Transformation?, step: Float): Transformation? {
            if (first == null) return second?.mul(step)
            if (second == null) return first.apply { mul(1f - step) }

            val tF = if (first.hasTranslation) first.translation else null
            val tS = if (second.hasTranslation) second.translation else null
            val rF = if (first.hasRotation) first.rotation else null
            val rS = if (second.hasRotation) second.rotation else null
            val sF = if (first.hasScale) first.scale else null
            val sS = if (second.hasScale) second.scale else null

            return Transformation(
                tF.lerp(tS, step),
                rF.lerp(rS, step),
                sF.lerp(sS, step)
            )
        }
    }
}

private fun Vector3f?.lerp(other: Vector3f?, factor: Float): Vector3f? {
    if (this == null) return other?.mul(factor)
    if (other == null) return this.mul(1f - factor)
    return this.lerp(other, factor)
}

private fun Quaternionf?.lerp(other: Quaternionf?, factor: Float): Quaternionf? {
    return when {
        this == null && other == null -> null
        this == null -> Quaternionf().slerp(other, factor)
        other == null -> Quaternionf().slerp(this, 1f - factor)
        else -> Quaternionf(this).slerp(other, factor)
    }
}