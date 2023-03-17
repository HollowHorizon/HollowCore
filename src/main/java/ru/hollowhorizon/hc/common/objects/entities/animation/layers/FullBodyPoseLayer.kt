package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.animation.WeightedAnimationBlend
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

class FullBodyPoseLayer<T>(name: String, animName: ResourceLocation, entity: T, val shouldLoop: Boolean) :
    LayerWithAnimation<T>(name, animName, entity) where T : Entity, T : IBTAnimatedEntity<T> {
    private var weightedBlend = WeightedAnimationBlend()

    override fun shouldLoop(): Boolean {
        return shouldLoop
    }

    override fun doLayerWork(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose) {
        val animation = getAnimation(BASE_SLOT)
        if (animation != null) {
            val ret = animation.getInterpolationFrames(currentTime - startTime, shouldLoop(), partialTicks)
            weightedBlend.simpleBlend(ret.current, ret.next, ret.partialTick)
            outPose.copyPose(weightedBlend.pose)
        }
    }
}