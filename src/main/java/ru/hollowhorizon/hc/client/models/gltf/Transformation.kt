package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f

/**
 * Class that represents a transformation that consists in translation, rotation and scale,
 * can be converted to a 4x4 matrix.
 */
data class Transformation(
    val translationX: Float,
    val translationY: Float,
    val translationZ: Float,
    val rotationX: Float,
    val rotationY: Float,
    val rotationZ: Float,
    val rotationW: Float,
    val scaleX: Float,
    val scaleY: Float,
    val scaleZ: Float
) {

    val translation: Vector3f get() = Vector3f(translationX, translationY, translationZ)
    val rotation: Quaternion get() = Quaternion(rotationX, rotationY, rotationZ, rotationW)
    val scale: Vector3f get() = Vector3f(scaleX, scaleY, scaleZ)

    @JvmOverloads
    constructor(
        translation: Vector3f = Vector3f(),
        rotation: Quaternion = Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
        scale: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)
    ) : this(
        translation.x(), translation.y(), translation.z(),
        rotation.i(), rotation.j(), rotation.k(), rotation.r(),
        scale.x(), scale.y(), scale.z()
    )

    /**
     * Converts this transformation into a Matrix
     * val matrix = getTransform(node, time).matrixVec.apply { transpose() }
    ForgeHooksClient.multiplyCurrentGlMatrix(matrix)
     */
    fun getMatrixVec(): Matrix4f {
        val m = Matrix4f()
        m.setIdentity()

        // rotation
        if (rotationW != 0f && !(rotationX == 0f && rotationY == 0f && rotationZ == 0f && rotationW == 1f)) {
            m.multiply(rotation.apply {
                //Invert
                val invNorm = 1.0f / (i() * i() + j() * j() + k() * k() + r() * r())
                set(i() * invNorm, j() * invNorm, k() * invNorm, r() * invNorm)
            })
        }
        // translation
        m.m30 = translationX
        m.m31 = translationY
        m.m32 = translationZ

        // scale
        m.m00 *= scaleX
        m.m01 *= scaleX
        m.m02 *= scaleX
        m.m10 *= scaleY
        m.m11 *= scaleY
        m.m12 *= scaleY
        m.m20 *= scaleZ
        m.m21 *= scaleZ
        m.m22 *= scaleZ

        return m
    }

    /**
     * Applies the translation, rotation, scale to OpenGL,
     * Equivalent to Gl11.glTranslatef(...), Gl11.glRotatef(...), Gl11.glScalef(...) but faster
     */
    fun glMultiply() {
        val matrix = getMatrixVec()
        matrix.transpose()
        RenderSystem.multMatrix(matrix)
    }

    /**
     * Converts this transformation into a Matrix
     */
    fun getMatrix4f(): Matrix4f = getMatrixVec()

    /**
     * Combines two transformations using matrix multiplication
     */
//    operator fun plus(other: TRSTransformation): TRSTransformation {
//        return this.getMatrixVec().apply {
//            multiply(other.getMatrix4f())
//        })
//    }

    /**
     * Linear interpolation of two transformations
     * @param step must be a value between 0.0 and 1.0 (both inclusive)
     */
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
