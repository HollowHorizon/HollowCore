package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.animation.WeightedAnimationBlend
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.ChangeBlendWeightMessage

open class BlendTwoPoseLayer<T>(
    name: String, anim1: ResourceLocation, anim2: ResourceLocation, entity: T,
    protected val shouldLoop: Boolean, protected open var blendAmount: Float,
) : LayerWithAnimation<T>(name, anim1, entity) where T : Entity, T : IBTAnimatedEntity<T> {
    private val anim1Blend: WeightedAnimationBlend = WeightedAnimationBlend()
    private val anim2Blend: WeightedAnimationBlend = WeightedAnimationBlend()
    private val finalBlend: WeightedAnimationBlend = WeightedAnimationBlend()

    init {
        setAnimation(anim2, SECOND_SLOT)
        addMessageCallback(ChangeBlendWeightMessage.CHANGE_BLEND_WEIGHT_TYPE) { message ->
            consumeChangeBlendWeight(
                message
            )
        }
    }

    override fun shouldLoop(): Boolean {
        return shouldLoop
    }

    private fun consumeChangeBlendWeight(message: AnimationLayerMessage) {
        if (message is ChangeBlendWeightMessage) {
            val changeMessage: ChangeBlendWeightMessage = message as ChangeBlendWeightMessage
            blendAmount = changeMessage.blendWeight
        }
    }

    override fun doLayerWork(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose) {
        val baseAnimation = getAnimation(BASE_SLOT)
        val blendAnimation = getAnimation(SECOND_SLOT)

        if(baseAnimation == null || blendAnimation == null) {
            outPose.copyPose(basePose)
            return
        }

        val ret = baseAnimation.getInterpolationFrames(
            currentTime - startTime, shouldLoop(), partialTicks
        )
        anim1Blend.simpleBlend(ret.current, ret.next, ret.partialTick)
        val ret2 = blendAnimation.getInterpolationFrames(
            currentTime - startTime, shouldLoop(), partialTicks
        )
        anim2Blend.simpleBlend(ret2.current, ret2.next, ret2.partialTick)
        finalBlend.simpleBlend(anim1Blend.pose, anim2Blend.pose, blendAmount)
        outPose.copyPose(finalBlend.pose)

    }

    companion object {
        const val SECOND_SLOT = "SECOND_SLOT"
    }
}