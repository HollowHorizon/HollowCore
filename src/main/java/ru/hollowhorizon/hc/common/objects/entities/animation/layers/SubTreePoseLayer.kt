package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.animation.LocalSubTreeBlend
import ru.hollowhorizon.hc.client.utils.math.Matrix4d
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

class SubTreePoseLayer<T>(
    name: String, animName: ResourceLocation, entity: T, private val shouldLoop: Boolean, boneName: String,
) : LayerWithAnimation<T>(name, animName, entity) where T : Entity, T : IBTAnimatedEntity<T> {
    private val skeleton = entity.skeleton
    private val boneIds = skeleton.getBoneIdsOfSubTree(boneName)
    private val localBlend = LocalSubTreeBlend(boneIds)

    override fun shouldLoop(): Boolean {
        return shouldLoop
    }

    override fun doLayerWork(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose) {
        val animation = getAnimation(BASE_SLOT)
        if (animation != null) {
            val ret = animation.getInterpolationFrames(
                currentTime - startTime, shouldLoop(), partialTicks
            )
            localBlend.setFrames(ret)
            val localPose: IPose = localBlend.pose
            for (id in boneIds!!) {
                val parentId: Int = skeleton.getBoneIdParentId(id)
                val parentGlobalLoc =
                    if (parentId != -1) outPose.getJointMatrix(parentId)
                    else Matrix4d()
                outPose.setJointMatrix(id, parentGlobalLoc)
                outPose.getJointMatrix(id).mulAffine(localPose.getJointMatrix(id))
            }
        }
    }
}