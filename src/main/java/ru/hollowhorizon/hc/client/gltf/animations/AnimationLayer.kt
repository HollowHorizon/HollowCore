package ru.hollowhorizon.hc.client.gltf.animations

import com.mojang.math.Quaternion
import de.javagl.jgltf.model.NodeModel
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.HollowCore


interface ILayer {
    var priority: Float

    fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray?

    fun update(partialTick: Float) {}
}

class AnimationLayer(val animation: Animation, override var priority: Float) : ILayer {
    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float) =
        animation.compute(node, target)

    override fun update(partialTick: Float) {
        animation.update(partialTick)
    }

}

class SmoothLayer(
    private var bindPose: Animation,
    private var first: Animation?,
    private var second: Animation?,
    override var priority: Float,
    private val switchSpeed: Float = 2.5f,
) : ILayer {
    private var switchPriority = 1.0f
    val current: Animation?
        get() = second

    override fun update(partialTick: Float) {
        first?.update(partialTick)
        second?.update(partialTick)
        if (switchPriority > 0f) {
            switchPriority -= (switchSpeed * partialTick) / 20f
            if (switchPriority < 0f) {
                HollowCore.LOGGER.info("COMPLETE!")
                switchPriority = 0f
            }
        }
    }

    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray? {
        val f = first?.hasNode(node, target) ?: false
        val s = second?.hasNode(node, target) ?: false

        return if (f && s) blend(node, target, first!!, second!!, partialTick)
        else if (s && switchPriority < 1f) second!!.compute(node, target)
        else if (f && switchPriority > 0f) blend(node, target, first!!, bindPose, partialTick)
        else null
    }

    private fun blend(
        node: NodeModel,
        target: AnimationTarget,
        first: Animation,
        second: Animation,
        time: Float,
    ): FloatArray? {
        val c1 = first.compute(node, target)
        val c2 = second.compute(node, target)

        return blend(c1, c2, switchPriority)
    }

    fun push(animation: Animation) {
        first = second
        second = animation
        switchPriority = 1.0f
    }
}

class HeadLayer(var animatable: LivingEntity, override var priority: Float) : ILayer {

    fun isValidNode(node: NodeModel?): Boolean {
        //если название кости head и её родители не имеют такого же название
        return node?.name?.contains("head", true) == true && !isValidNode(node.parent)
    }

    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray? {
        if (target == AnimationTarget.ROTATION && isValidNode(node)) {
            val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
            val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
            val netHeadYaw = headYaw - bodyYaw
            val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)
            val rot = Quaternion(headPitch, netHeadYaw, 0f, true)

            return floatArrayOf(rot.i(), rot.j(), rot.k(), rot.r())
        }
        return null
    }

}

private fun blend(first: FloatArray?, second: FloatArray?, factor: Float): FloatArray? {
    return if (first == null && second == null) null
    else if (first != null && second == null || factor == 1f) first
    else if (first == null || factor == 0f) second
    else first.apply {
        for (i in this.indices) this[i] = this[i] * factor + second!![i] * (1.0f - factor)
    }
}