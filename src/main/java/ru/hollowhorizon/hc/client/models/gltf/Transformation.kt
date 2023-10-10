package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import org.lwjgl.opengl.GL11
import java.nio.FloatBuffer

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
    private val matrix: Matrix4f = Matrix4f().apply(Matrix4f::setIdentity)
) {

    val translation: Vector3f get() = Vector3f(translationX, translationY, translationZ)
    val rotation: Quaternion get() = Quaternion(rotationX, rotationY, rotationZ, rotationW)
    val scale: Vector3f get() = Vector3f(scaleX, scaleY, scaleZ)

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
        translationX = transformation.translationX
        translationY = transformation.translationY
        translationZ = transformation.translationZ
        rotationX = transformation.rotationX
        rotationY = transformation.rotationY
        rotationZ = transformation.rotationZ
        rotationW = transformation.rotationW
        scaleX = transformation.scaleX
        scaleY = transformation.scaleY
        scaleZ = transformation.scaleZ
        matrix.setIdentity()
        matrix.multiply(transformation.matrix)
    }

    @JvmOverloads
    constructor(
        translation: Vector3f = Vector3f(),
        rotation: Quaternion = Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
        scale: Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
        matrix: Matrix4f = Matrix4f().apply(Matrix4f::setIdentity)
    ) : this(
        translation.x(), translation.y(), translation.z(),
        rotation.i(), rotation.j(), rotation.k(), rotation.r(),
        scale.x(), scale.y(), scale.z(),
        matrix
    )

    fun getMatrix(): Matrix4f {
        val m = matrix.copy()

        val t = Matrix4f.createTranslateMatrix(translationX, translationY, translationZ)
        val r = Matrix4f(Quaternion(rotationX, rotationY, rotationZ, rotationW))
        val s = Matrix4f.createScaleMatrix(scaleX, scaleY, scaleZ)

        m.multiply(t)
        m.multiply(r)
        m.multiply(s)

        return m
    }

    fun lerp(other: Transformation, step: Float): Transformation {
        return Transformation(
            translation = this.translation.interpolated(other.translation, step),
            rotation = this.rotation.interpolated(other.rotation, step),
            scale = this.scale.interpolated(other.scale, step)
        )
    }

    companion object {
        val IDENTITY: Transformation = Transformation()
    }
}

private fun Vector3f.interpolated(other: Vector3f, step: Float): Vector3f {
    return Vector3f(
        this.x() + (other.x() - this.x()) * step,
        this.y() + (other.y() - this.y()) * step,
        this.z() + (other.z() - this.z()) * step,
    )
}

private fun Quaternion.interpolated(other: Quaternion, step: Float): Quaternion {
    return Quaternion(
        this.i() + (other.i() - this.i()) * step,
        this.j() + (other.j() - this.j()) * step,
        this.k() + (other.k() - this.k()) * step,
        this.r() + (other.r() - this.r()) * step,
    )
}
