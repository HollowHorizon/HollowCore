package ru.hollowhorizon.hc.client.models.gltf.animations.interpolations

import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import java.util.*

abstract class Interpolator<T>(val keys: FloatArray, val values: Array<T>) {
    abstract fun compute(time: Float): T

    val maxTime = keys.last()

    val Float.animIndex: Int
        get() {
            val index = Arrays.binarySearch(keys, this)

            return if (index >= 0) index
            else 0.coerceAtLeast(-index - 2)
        }

    lateinit var node: GltfTree.Node
}