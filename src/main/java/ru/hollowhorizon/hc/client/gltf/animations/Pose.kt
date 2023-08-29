package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel

data class Pose(val joints: List<Joint>) {
    val size = joints.size

    fun apply(model: List<NodeModel>) {
        model.forEachIndexed { i, node -> joints[i].apply(node) }
    }

    companion object {
        fun createByNodes(model: List<NodeModel>) = Pose(model.map { Joint.create(it) })

        fun blend(first: Pose, second: Pose, factor: Float): Pose {
            return Pose(first.joints.map { j1 ->
                val j2 = second.joints.find { j2 -> j2.name == j1.name } ?: j1

                return@map Joint.blend(j1, j2, factor)
            })
        }
    }
}

class Joint(
    val name: String?,
    val tX: Float, val tY: Float, val tZ: Float,
    val rX: Float, val rY: Float, val rZ: Float, val rW: Float,
    val sX: Float, val sY: Float, val sZ: Float,
    val weight: Float = 0f,
) {
    companion object {
        fun create(node: NodeModel): Joint {
            val t = node.translation ?: floatArrayOf(0f, 0f, 0f)
            val r = node.rotation ?: floatArrayOf(0f, 0f, 0f, 0f)
            val s = node.scale ?: floatArrayOf(0f, 0f, 0f)
            val w = node.weights?.get(0) ?: 0f
            return Joint(
                node.name,
                t[0], t[1], t[2],
                r[0], r[1], r[2], r[3],
                s[0], s[1], s[2], w
            )
        }

        fun blend(j1: Joint, j2: Joint, factor: Float): Joint {
            return Joint(
                j1.name,
                b(j1.tX, j2.tX, factor),
                b(j1.tY, j2.tY, factor),
                b(j1.tZ, j2.tZ, factor),

                b(j1.rX, j2.rX, factor),
                b(j1.rY, j2.rY, factor),
                b(j1.rZ, j2.rZ, factor),
                b(j1.rW, j2.rW, factor),

                b(j1.sX, j2.sX, factor),
                b(j1.sY, j2.sY, factor),
                b(j1.sZ, j2.sZ, factor),

                b(j1.weight, j2.weight, factor)
            )
        }

        private fun b(v1: Float, v2: Float, factor: Float) = v1 * (1 - factor) + v2 * factor
    }

    fun translation() = floatArrayOf(tX, tY, tZ)
    fun rotation() = floatArrayOf(rX, rY, rZ, rW)
    fun scale() = floatArrayOf(sX, sY, sZ)
    fun weights() = floatArrayOf(weight)
    fun apply(node: NodeModel) {
        node.translation = translation()
        node.rotation = rotation()
        node.scale = scale()
        node.weights = weights()
    }
}