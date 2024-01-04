package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.minecraft.util.Mth
import kotlin.math.acos


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
    private val matrix: Matrix4f = Matrix4f().apply(Matrix4f::setIdentity),
) {

    val translation: Vector3f get() = Vector3f(translationX, translationY, translationZ)
    val rotation: Quaternion get() = Quaternion(rotationX, rotationY, rotationZ, rotationW)
    val scale: Vector3f get() = Vector3f(scaleX, scaleY, scaleZ)

    fun add(transform: Transformation) {

        if (transform.hasTranslation) {
            translationX += transform.translationX
            translationY += transform.translationY
            translationZ += transform.translationZ
        }

        if (transform.hasRotation) {
            val res = rotation.apply { mul(transform.rotation) }
            rotationX = res.i()
            rotationY = res.j()
            rotationZ = res.k()
            rotationW = res.r()
        }

        if (transform.hasScale) {
            scaleX *= transform.scaleX
            scaleY *= transform.scaleY
            scaleZ *= transform.scaleZ
        }
    }

    fun sub(transform: Transformation) {
        translationX -= transform.translationX
        translationY -= transform.translationY
        translationZ -= transform.translationZ

        val res = rotation.apply {
            val other = transform.rotation
            other.invert()
            mul(other)
        }
        rotationX = res.i()
        rotationY = res.j()
        rotationZ = res.k()
        rotationW = res.r()

        scaleX /= transform.scaleX
        scaleY /= transform.scaleY
        scaleZ /= transform.scaleZ
    }

    fun mul(factor: Float) {
        translationX *= factor
        translationY *= factor
        translationZ *= factor

        val q = Quaternion.ONE.copy().lerp(rotation, factor)
        rotationX = q?.i() ?: rotationX
        rotationY = q?.j() ?: rotationY
        rotationZ = q?.k() ?: rotationZ
        rotationW = q?.r() ?: rotationW

        scaleX = scaleX * factor + 1f * (1f - factor)
        scaleY = scaleY * factor + 1f * (1f - factor)
        scaleZ = scaleZ * factor + 1f * (1f - factor)
    }

    fun setTranslation(array: FloatArray?) {
        if (array == null) return
        translationX = array[0]
        translationY = array[1]
        translationZ = array[2]
    }

    fun setRotation(array: FloatArray?) {
        if (array == null) return
        rotationX = array[0]
        rotationY = array[1]
        rotationZ = array[2]
        rotationW = array[3]
    }

    fun setScale(array: FloatArray?) {
        if (array == null) return
        scaleX = array[0]
        scaleY = array[1]
        scaleZ = array[2]
    }

    fun set(transformation: Transformation) {
        if (hasTranslation) {
            translationX = transformation.translationX
            translationY = transformation.translationY
            translationZ = transformation.translationZ
        }
        if (hasRotation) {
            rotationX = transformation.rotationX
            rotationY = transformation.rotationY
            rotationZ = transformation.rotationZ
            rotationW = transformation.rotationW
        }
        if (hasScale) {
            scaleX = transformation.scaleX
            scaleY = transformation.scaleY
            scaleZ = transformation.scaleZ
        }
    }

    fun set(transformationMap: Map<Transformation, Float>) {
        if (transformationMap.isEmpty()) return
        translationX = transformationMap.sumComponent { it.translationX }
        translationY = transformationMap.sumComponent { it.translationY }
        translationZ = transformationMap.sumComponent { it.translationZ }


        rotationX = transformationMap.sumComponent { it.rotationX }
        rotationY = transformationMap.sumComponent { it.rotationY }
        rotationZ = transformationMap.sumComponent { it.rotationZ }
        rotationW = transformationMap.sumComponent { it.rotationW }

        scaleX = transformationMap.sumComponent { it.scaleX }
        scaleY = transformationMap.sumComponent { it.scaleY }
        scaleZ = transformationMap.sumComponent { it.scaleZ }
    }

    @JvmOverloads
    constructor(
        translation: Vector3f? = Vector3f(),
        rotation: Quaternion? = Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
        scale: Vector3f? = Vector3f(1.0f, 1.0f, 1.0f),
        matrix: Matrix4f = Matrix4f().apply(Matrix4f::setIdentity),
    ) : this(
        translation?.x() ?: 0f, translation?.y() ?: 0f, translation?.z() ?: 0f,
        rotation?.i() ?: 0f, rotation?.j() ?: 0f, rotation?.k() ?: 0f, rotation?.r() ?: 1f,
        scale?.x() ?: 1f, scale?.y() ?: 1f, scale?.z() ?: 1f,
        translation != null, rotation != null, scale != null,
        matrix
    )

    fun getMatrix(): Matrix4f {
        val m = Matrix4f()
        m.setIdentity()

        val t = Matrix4f.createTranslateMatrix(translationX, translationY, translationZ)
        val r = Matrix4f(Quaternion(rotationX, rotationY, rotationZ, rotationW))
        val s = Matrix4f.createScaleMatrix(scaleX, scaleY, scaleZ)

        m.multiply(t)
        m.multiply(r)
        m.multiply(s)
        m.multiply(matrix)

        return m
    }

    companion object {
        val IDENTITY: Transformation = Transformation()

        fun lerp(first: Transformation?, second: Transformation?, step: Float): Transformation? {
            if (first == null) return second?.apply { mul(step) }
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

private fun Quaternion.invert() {
    val factor = 1f / (i() * i() + j() * j() + k() * k() + r() * r())
    set(
        -i() * factor,
        -j() * factor,
        -k() * factor,
        r() * factor
    )
}

private fun Vector3f?.lerp(other: Vector3f?, factor: Float): Vector3f? {
    if (this == null) return other?.apply { mul(factor) }
    if (other == null) return this.apply { mul(1f - factor) }
    this.lerp(other, factor)
    return this
}

private fun Quaternion?.lerp(other: Quaternion?, factor: Float): Quaternion? {
    if (this == null) return other?.apply { mul(factor) }
    if (other == null) return this.apply { mul(1f - factor) }

    return this.interpolated(other, factor)
}

private fun Map<Transformation, Float>.sumComponent(component: (Transformation) -> Float): Float {
    var sum = 0f
    this.entries.forEach { (transformation, value) ->
        sum += value * component(transformation)
    }
    return sum / this.values.sum()
}

private fun Vector3f.interpolated(other: Vector3f, step: Float): Vector3f {
    return Vector3f(
        this.x() + (other.x() - this.x()) * step,
        this.y() + (other.y() - this.y()) * step,
        this.z() + (other.z() - this.z()) * step,
    )
}

private fun Quaternion.interpolated(other: Quaternion, alpha: Float): Quaternion {
    val beginX = this.i()
    val beginY = this.j()
    val beginZ = this.k()
    val beginW = this.r()
    var endX = other.i()
    var endY = other.j()
    var endZ = other.k()
    var endW = other.r()

    var dot = beginX * endX + beginY * endY + beginZ * endZ + beginW * endW
    if (dot < 0) {
        endX = -endX
        endY = -endY
        endZ = -endZ
        endW = -endW
        dot = -dot
    }
    val epsilon = 1e-6f
    val s0: Float
    val s1: Float
    if (1.0 - dot > epsilon) {
        val omega = acos(dot)
        val invSinOmega = 1.0f / Mth.sin(omega)
        s0 = Mth.sin((1.0f - alpha) * omega) * invSinOmega
        s1 = Mth.sin(alpha * omega) * invSinOmega
    } else {
        s0 = 1.0f - alpha
        s1 = alpha
    }

    return Quaternion(
        s0 * beginX + s1 * endX,
        s0 * beginY + s1 * endY,
        s0 * beginZ + s1 * endZ,
        s0 * beginW + s1 * endW
    )
}