package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.NodeModel
import ru.hollowhorizon.hc.HollowCore

interface ILayer {
    var priority: Float

    fun compute(node: NodeModel, target: AnimationTarget, time: Float): FloatArray?

    fun update(partialTick: Float) {}
}

class AnimationLayer(val animation: Animation, override var priority: Float) : ILayer {
    override fun compute(node: NodeModel, target: AnimationTarget, time: Float) =
        animation.compute(node, target, time % animation.maxTime)

}

class SmoothLayer(
    private var bindPose: Animation,
    private var first: Animation?,
    private var second: Animation?,
    override var priority: Float,
    private val switchSpeed: Float = 2.0f
) : ILayer {
    private var switchPriority = 1.0f
    val current: Animation?
        get() = second

    override fun update(partialTick: Float) {
        if (switchPriority > 0f) {
            switchPriority -= (switchSpeed * partialTick) / 20f
            if (switchPriority < 0f) {
                HollowCore.LOGGER.info("COMPLETE!")
                switchPriority = 0f
            }
        }
    }

    override fun compute(node: NodeModel, target: AnimationTarget, time: Float): FloatArray? {
        val f = first?.hasNode(node, target) ?: false
        val s = second?.hasNode(node, target) ?: false

        return if(f && s) blend(node, target, first!!, second!!, time)
        else if(s && switchPriority < 1f) second!!.compute(node, target, time % second!!.maxTime)
        else if(f && switchPriority > 0f) blend(node, target, first!!, bindPose, time)
        else null
    }

    private fun blend(
        node: NodeModel,
        target: AnimationTarget,
        first: Animation,
        second: Animation,
        time: Float
    ): FloatArray? {
        val c1 = first.compute(node, target, time % first.maxTime)
        val c2 = second.compute(node, target, time % second.maxTime)

        return blend(c1, c2, switchPriority)
    }

    fun push(animation: Animation) {
        first = second
        second = animation
        switchPriority = 1.0f
        HollowCore.LOGGER.info("RESET!")
    }
}

private fun blend(first: FloatArray?, second: FloatArray?, factor: Float): FloatArray? {
    return if (first == null && second == null) null
    else if(first != null && second == null || factor == 1f) first
    else if(first == null || factor == 0f) second
    else first.apply {
        for(i in this.indices) this[i] = this[i] * factor + second!![i] * (1.0f - factor)
    }
}