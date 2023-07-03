package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel

class AnimationFrame(
    var translation: FloatArray = FloatArray(3),
    var rotation: FloatArray = FloatArray(4),
    var scale: FloatArray = FloatArray(3),
    var weights: FloatArray = FloatArray(1)
) {
    companion object {
        fun compute(frames: List<Pair<Float, AnimationFrame>>): AnimationFrame {
            val firstFrame = frames.first().second
            val prioritySum = frames.map { it.first }.sum()

            val translation = FloatArray(firstFrame.translation.size) { 0f }
            val rotation = FloatArray(firstFrame.rotation.size) { 0f }
            val scale = FloatArray(firstFrame.scale.size) { 0f }
            val weights = FloatArray(firstFrame.weights.size) { 0f }

            frames.forEach { (priority, frame) ->
                translation.add(frame.translation * priority)
                rotation.add(frame.rotation * priority)
                scale.add(frame.scale * priority)
                weights.add(frame.weights * priority)
            }

            return AnimationFrame(
                translation / prioritySum,
                rotation / prioritySum,
                scale / prioritySum,
                weights / prioritySum
            )
        }

        fun blend(first: AnimationFrame, second: AnimationFrame, factor: Float): AnimationFrame {
            return AnimationFrame(
                blendArray(first.translation, second.translation, factor),
                blendArray(first.rotation, second.rotation, factor),
                blendArray(first.scale, second.scale, factor),
                blendArray(first.weights, second.weights, factor)
            )
        }

        private fun blendArray(first: FloatArray, second: FloatArray, factor: Float): FloatArray {
            val result = FloatArray(first.size)
            for (i in second.indices) {
                result[i] = first[i] * (1 - factor) + second[i] * factor
            }
            return result
        }
    }

    fun apply(node: NodeModel) {
        System.arraycopy(translation, 0, node.translation, 0, translation.size)
        System.arraycopy(rotation, 0, node.rotation, 0, rotation.size)
        System.arraycopy(scale, 0, node.scale, 0, scale.size)
        System.arraycopy(weights, 0, node.weights, 0, weights.size)
    }
}

private fun FloatArray.add(floats: FloatArray): FloatArray {
    for (i in this.indices) this[i] = this[i] + floats[i]
    return this
}

private operator fun FloatArray.div(value: Float): FloatArray {
    for (i in this.indices) this[i] = this[i] / value
    return this
}

private operator fun FloatArray.times(priority: Float): FloatArray {
    for (i in this.indices) this[i] = this[i] * priority
    return this
}