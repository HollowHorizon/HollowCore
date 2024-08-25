package ru.hollowhorizon.hc.client.models.internal

import org.joml.Matrix4f
import java.util.HashMap

class Skin(
    val jointsIds: List<Int>,
    val inverseBindMatrices: Array<Matrix4f>,
) {
    val joints = HashMap<Int, Node>(jointsIds.size)

    private val skin = Array(jointsIds.size) { Matrix4f() }

    fun finalMatrices(node: Node): Array<Matrix4f> {
        // Получаем и инвертируем глобальную матрицу узла модели
        val inverseTransform = Matrix4f(node.globalMatrix).invert()

        // Проходим по всем суставам
        for (i in jointsIds.indices) {
            val jointGlobalMatrix = joints[i]!!.globalMatrix
            val bindMatrix = Matrix4f(inverseBindMatrices[i]).transpose()
            val skinMatrix = Matrix4f(jointGlobalMatrix).mul(bindMatrix)
            skin[i] = Matrix4f(inverseTransform).mul(skinMatrix)
        }
        return skin
    }
}