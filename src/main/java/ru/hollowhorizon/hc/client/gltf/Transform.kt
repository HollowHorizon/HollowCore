package ru.hollowhorizon.hc.client.gltf

import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import kotlinx.serialization.Serializable

@Serializable
data class Transform(
    var tX: Float = 0f, var tY: Float = 0f, var tZ: Float = 0f,
    var rX: Float = 0f, var rY: Float = 0f, var rZ: Float = 0f,
    var sX: Float = 1.0f, var sY: Float = 1.0f, var sZ: Float = 1.0f,
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
}