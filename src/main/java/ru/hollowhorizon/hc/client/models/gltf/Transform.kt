package ru.hollowhorizon.hc.client.models.gltf

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