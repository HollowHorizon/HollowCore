package ru.hollowhorizon.hc.client.gltf.animations

import com.mojang.math.Quaternion
import de.javagl.jgltf.model.NodeModel
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity


interface ILayer {
    var priority: Float
    var playType: PlayType
    var speed: Float
    var shouldRemove: Boolean

    fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray?

    fun update(manager: GLTFAnimationManager, partialTick: Float) {}
}

class AnimationLayer(
    val animation: Animation, override var priority: Float,
    override var playType: PlayType = PlayType.ONCE,
    override var speed: Float = 1.0f,
) : ILayer {
    override var shouldRemove = false

    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float) =
        animation.compute(node, target)

    override fun update(manager: GLTFAnimationManager, partialTick: Float) {
        animation.update(this, manager, partialTick)
    }

}

class SmoothLayer(
    private var bindPose: Animation,
    private var first: Animation?,
    var second: Animation?,
    override var priority: Float,
    private val switchSpeed: Float = 0.5f,
    override var playType: PlayType = PlayType.LOOPED,
    override var speed: Float = 1.0f,
) : ILayer {
    private var switchPriority = 1.0f
    override var shouldRemove = false
    var shouldUpdate = false

    val current: Animation?
        get() = second

    override fun update(manager: GLTFAnimationManager, partialTick: Float) {
        first?.update(this, manager, partialTick)
        second?.update(this, manager, partialTick)
        if(second?.isEnded == true) {
            shouldUpdate = true
        }
        if (switchPriority > 0f) {
            val currentTicks = (manager.currentTick - (second?.startTime ?: 0) + partialTick) / 20f
            switchPriority = 1.0f - currentTicks / switchSpeed
            if (switchPriority < 0f) switchPriority = 0f
        }
    }

    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray? {
        val f = first?.hasNode(node, target) ?: false
        val s = second?.hasNode(node, target) ?: false

        return if (f && s) blend(node, target, first!!, second!!)
        else if (s) blend(node, target, bindPose, second!!)
        else if (f) blend(node, target, first!!, bindPose)
        else null
    }

    private fun blend(
        node: NodeModel,
        target: AnimationTarget,
        first: Animation,
        second: Animation,
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
    override var playType = PlayType.LOOPED
    override var speed = 1.0f
    override var shouldRemove = false

    fun isValidNode(node: NodeModel?): Boolean {
        //если название кости head и её родители не имеют такого же название
        return node?.name?.contains("head", true) == true && !isValidNode(node.parent)
    }

    override fun compute(node: NodeModel, target: AnimationTarget, partialTick: Float): FloatArray? {
        if (target == AnimationTarget.ROTATION && isValidNode(node)) {
            val bodyYaw = -Mth.rotLerp(partialTick * 20f, animatable.yBodyRotO, animatable.yBodyRot)
            val headYaw = -Mth.rotLerp(partialTick * 20f, animatable.yHeadRotO, animatable.yHeadRot)
            val netHeadYaw = headYaw - bodyYaw
            val headPitch = -Mth.rotLerp(partialTick * 20f, animatable.xRotO, animatable.xRot)
            val rot = Quaternion(headPitch, netHeadYaw, 0f, true)

            return floatArrayOf(rot.i(), rot.j(), rot.k(), rot.r())
        }
        return null
    }

}

private fun blend(first: FloatArray?, second: FloatArray?, factor: Float): FloatArray? {
    return when {
        first == null && second == null -> null
        first != null && second == null || factor == 1f -> first
        first == null || factor == 0f -> second
        else -> first.copyOf().apply {
            for (i in this.indices) this[i] = this[i] * factor + second!![i] * (1.0f - factor)
        }
    }
}