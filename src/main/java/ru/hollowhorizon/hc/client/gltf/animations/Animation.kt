package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel

class Animation(val animationData: Map<NodeModel, List<AnimationData>>) {
    fun compute(node: NodeModel, time: Float): Map<AnimationTarget, FloatArray> {
        return animationData[node]?.associate { anim ->
            anim.target to anim.interpolator.compute(time)
        } ?: emptyMap()
    }
}

class AnimationData(val target: AnimationTarget, val interpolator: Interpolator<*>)

enum class AnimationTarget { TRANSLATION, ROTATION, SCALE, WEIGHTS }