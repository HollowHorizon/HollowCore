package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.sphericalLerp
import kotlin.math.abs


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
            val res = rotation
            //Рубрика: угадай сколько часов потребовалось чтобы понять, что нужно использовать этот метод
            res.mulLeft(transform.rotation)
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

        hasTranslation = transform.hasTranslation
        hasRotation = transform.hasRotation
        hasScale = transform.hasScale
    }

    fun Quaternion.mulLeft(q: Quaternion) {
        set(
            q.r() * i() + q.i() * r() + q.j() * k() - q.k() * j(),
            q.r() * j() - q.i() * k() + q.j() * r() + q.k() * i(),
            q.r() * k() + q.i() * j() - q.j() * i() + q.k() * r(),
            q.r() * r() - q.i() * i() - q.j() * j() - q.k() * k()
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
            res.mul(rotation.apply { invert() })
            rotationX = res.i()
            rotationY = res.j()
            rotationZ = res.k()
            rotationW = res.r()
        }

        if (transform.hasScale) {
            scaleX = transform.scaleX / scaleX
            scaleY = transform.scaleY / scaleY
            scaleZ = transform.scaleZ / scaleZ
        }

        hasTranslation = transform.hasTranslation
        hasRotation = transform.hasRotation
        hasScale = transform.hasScale
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

    fun setTranslation(array: Vector3f?) {
        if (array == null) return
        translationX = array.x()
        translationY = array.y()
        translationZ = array.z()
    }

    fun setRotation(array: Quaternion?) {
        if (array == null) return
        rotationX = array.i()
        rotationY = array.j()
        rotationZ = array.k()
        rotationW = array.r()
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

    fun setLocal(node: GltfTree.Node, animPose: Transformation) = set(node.fromLocal(animPose))

    override fun equals(other: Any?): Boolean {
        if (other is Transformation) {
            if(hasTranslation) {
                if(translationX != other.translationX) return false
                if(translationY != other.translationY) return false
                if(translationZ != other.translationZ) return false
            }
            if(hasRotation) {
                if(abs(rotationY - other.rotationY) > 0.1) return false
                if(abs(rotationZ - other.rotationZ) > 0.1) return false
                if(abs(rotationX - other.rotationX) > 0.1) return false
                if(abs(rotationW - other.rotationW) > 0.1) return false
            }
            if(hasScale) {
                if(scaleX != other.scaleX) return false
                if(scaleY != other.scaleY) return false
                if(scaleZ != other.scaleZ) return false
            }
            return true
        } else return super.equals(other)
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
    val factor = i() * i() + j() * j() + k() * k() + r() * r()
    set(
        -i() / factor,
        -j() / factor,
        -k() / factor,
        r() / factor
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

    val r = this.copy()
    r.sphericalLerp(other, factor)
    return r
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