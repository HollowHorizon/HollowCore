package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel
import net.minecraft.entity.LivingEntity

interface ILayer {
    val priority: Float

    fun compute(node: NodeModel, time: Float): Map<AnimationTarget, FloatArray>
    fun update(partialTick: Float) {}
}

class AnimationLayer(val animation: Animation, override val priority: Float) : ILayer {
    override fun compute(node: NodeModel, time: Float) =
        animation.compute(node, priority, time % animation.maxTime)
}

class CodeLayer(
    val action: (NodeModel) -> Map<AnimationTarget, FloatArray>,
    override val priority: Float
) : ILayer {
    override fun compute(node: NodeModel, time: Float) = action(node)

}

class SmoothLayer(
    private var first: Animation?,
    private var second: Animation?,
    override val priority: Float,
    private val switchTime: Float = 0.5f
) : ILayer {
    private var switchPriority = 1.0f
    val current: Animation?
        get() = second

    override fun update(partialTick: Float) {
        if (switchPriority > 0f) {
            switchPriority -= (switchTime * partialTick) / 60f
            if (switchPriority < 0f) switchPriority = 0f
        }
    }

    override fun compute(node: NodeModel, time: Float): Map<AnimationTarget, FloatArray> {
        val f = first?.hasNode(node)
        val s = second?.hasNode(node)
        if (f != s) {
            return if (f == true) {
                emptyMap()
            } else {
                second!!.compute(node, 1.0f, time % second!!.maxTime)
            }
        }

        val c1 = first?.compute(node, switchPriority, time % first!!.maxTime) ?: emptyMap()
        val c2 = second?.compute(node, 1.0f - switchPriority, time % second!!.maxTime) ?: emptyMap()

        return listOf(c1, c2).flatMap { it.entries }.groupBy({ it.key }, { it.value })
            .map { it.key to it.value.sumWithPriority(1.0f) }.toMap()
    }

    fun push(animation: Animation) {
        first = second
        second = animation
        switchPriority = 1.0f
    }
}