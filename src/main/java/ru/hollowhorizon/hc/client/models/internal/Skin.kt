package ru.hollowhorizon.hc.client.models.internal

import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.MutableMat4f
import org.joml.Matrix4f

class Skin(
    val jointsIds: List<Int>,
    val inverseBindMatrices: Array<Matrix4f>,
) {
    val joints = HashMap<Int, Node>(jointsIds.size)

    private val skin = Array(jointsIds.size) { Matrix4f() }

    fun finalMatrices(node: Node): Array<Matrix4f> {
        // Получаем и инвертируем глобальную матрицу узла модели
        val inverseRoot = MutableMat4f(node.globalMatrix).transpose()
        inverseRoot.invert()

        // Проходим по всем суставам
        for (i in jointsIds.indices) {
            val jointGlobalMatrix = MutableMat4f(joints[i]!!.globalMatrix).transpose()
            val bindMatrix = MutableMat4f(inverseBindMatrices[i])
            val skinMatrix = MutableMat4f(jointGlobalMatrix).mul(bindMatrix)
            skin[i] = MutableMat4f(inverseRoot).mul(skinMatrix).transpose().toMatrix()
        }
        return skin
    }

    fun MutableMat4f(m: Matrix4f) = MutableMat4f(
        m.m00(), m.m01(), m.m02(), m.m03(),
        m.m10(), m.m11(), m.m12(), m.m13(),
        m.m20(), m.m21(), m.m22(), m.m23(),
        m.m30(), m.m31(), m.m32(), m.m33(),
    )

    fun Mat4f.toMatrix(): Matrix4f {
        return Matrix4f(
            m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33
        )
    }
}