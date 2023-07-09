package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel

class AnimationLayer(var priority: Float, val animation: Animation) {

    init {
        if (priority !in 0.0f..1.0f) throw AnimationException("Animation priority must be in range [0.0, 1.0]")
    }

    fun shouldApply(node: NodeModel): Boolean = animation.shouldApply(node)

    fun preUpdate() {

    }

    fun update(node: NodeModel, partialTick: Float): AnimationFrame =
        animation.compute(node, partialTick)
}