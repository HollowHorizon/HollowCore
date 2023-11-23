package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec2
import org.lwjgl.opengl.GL11
import java.nio.FloatBuffer
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
    private val matrix: Matrix4f = Matrix4f().apply(Matrix4f::setIdentity)
) {
    var hasChanged = true
    val bindTranslation = Vector3f(translationX, translationY, translationZ)
    val bindRotation = Quaternion(rotationX, rotationY, rotationZ, rotationW)
    val bindScale = Vector3f(scaleX, scaleY, scaleZ)


    val translation: Vector3f get() = Vector3f(translationX, translationY, translationZ)
    val rotation: Quaternion get() = Quaternion(rotationX, rotationY, rotationZ, rotationW)
    val scale: Vector3f get() = Vector3f(scaleX, scaleY, scaleZ)

    fun setTranslation(array: FloatArray?) {
        if (array == null) return
        hasChanged = true
        translationX = array[0]
        translationY = array[1]
        translationZ = array[2]
    }

    fun setRotation(array: FloatArray?) {
        if (array == null) return
        hasChanged = true
        rotationX = array[0]
        rotationY = array[1]
        rotationZ = array[2]
        rotationW = array[3]
    }

    fun setScale(array: FloatArray?) {
        if (array == null) return
        hasChanged = true
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
        hasChanged = true
    }

    fun set(transformationMap: Map<Transformation, Float>) {
        if(transformationMap.isEmpty()) return
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
        hasChanged = true
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

    companion object {
        val IDENTITY: Transformation = Transformation()

        fun lerp(first: Transformation, second: Transformation, step: Float): Transformation {
            return Transformation(
                translation = first.translation.interpolated(second.translation, step),
                rotation = first.rotation.interpolated(second.rotation, step),
                scale = first.scale.interpolated(second.scale, step)
            )
        }
    }
}

private fun Map<Transformation, Float>.sumComponent(component: (Transformation) -> Float): Float {
    var sum = 0f
    this.entries.forEach { (transformation, priority) ->
        sum += priority * component(transformation)
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
    if(dot < 0) {
        endX = -endX
        endY = -endY
        endZ = -endZ
        endW = -endW
        dot = -dot
    }
    val epsilon = 1e-6f
    val s0: Float
    val s1: Float
    if(1.0 - dot > epsilon) {
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
        s0 * beginW + s1 * endW)
}