package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Vector2f
import net.minecraft.util.math.vector.Vector3d


class Vertex(val position: Vector3d, val normal: Vector3d, val tex: Vector2f) {
    val bones: IntArray = IntArray(4)
    val weights: FloatArray = FloatArray(4)

    init {
        for (i in 0..3) {
            bones[i] = -1
            weights[i] = 0.0f
        }
    }
}