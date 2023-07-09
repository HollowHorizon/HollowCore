package ru.hollowhorizon.hc.client.gltf.animations

import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel

class AnimationManager(val model: RenderedGltfModel) {
    private val nodeModels = model.gltfModel.nodeModels
    private val layers = ArrayList<AnimationLayer>()

    fun update(partialTick: Float) {
        layers.forEach(AnimationLayer::preUpdate)
        layers.removeIf { it.priority < 0.0f }

        layers.forEach { layer ->

        }

        nodeModels.forEach { node ->
            val frames = layers.filter { it.shouldApply(node) }.map { it.priority to it.update(node, partialTick) }

            AnimationFrame.compute(frames).apply(node)
        }
    }

    fun addLayer(layer: AnimationLayer) = layers.add(layer)

    fun addLayer(priority: Float, animation: Animation) = addLayer(AnimationLayer(priority, animation))

    fun addLayer(animation: Animation) = addLayer(0.5f, animation)

    fun removeLayer(layer: AnimationLayer) = layers.remove(layer)
}
