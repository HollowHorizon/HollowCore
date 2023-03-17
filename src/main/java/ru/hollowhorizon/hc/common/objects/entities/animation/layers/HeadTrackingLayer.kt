package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.utils.math.Matrix4d
import ru.hollowhorizon.hc.client.utils.math.Vector4d
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

class HeadTrackingLayer<T>(name: String, entity: T, val boneName: String) :
    AnimationLayerBase<T>(name, entity) where T : LivingEntity, T : IBTAnimatedEntity<T> {
    var rotLimit = 85.0f

    override fun doLayerWork(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose) {
        val skeleton = entity.skeleton
        val bone = skeleton.getBone(boneName)
        if (bone != null) {
            var bodyYaw = MathHelper.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot)
            val headYaw = MathHelper.lerp(partialTicks, entity.yHeadRotO, entity.yHeadRot)
            val shouldSit = entity.isPassenger && entity.vehicle != null && entity.vehicle!!.shouldRiderSit()
            var netHeadYaw = headYaw - bodyYaw
            if (shouldSit && entity.vehicle is LivingEntity) {
                val livingentity = entity.vehicle as LivingEntity
                bodyYaw = MathHelper.lerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot)
                netHeadYaw = headYaw - bodyYaw
                var wrapped = MathHelper.wrapDegrees(netHeadYaw)
                wrapped = MathHelper.clamp(wrapped, -rotLimit, rotLimit)

                bodyYaw = headYaw - wrapped
                if (wrapped * wrapped > 2500.0f) bodyYaw += wrapped * 0.2f

                netHeadYaw = headYaw - bodyYaw
            }
            val headPitch = MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)
            val rotateY = netHeadYaw * (Math.PI.toFloat() / 180f)
            val isFlying: Boolean = entity.fallFlyingTicks > 4
            val rotateX =
                if (isFlying) -Math.PI.toFloat() / 4f
                else headPitch * (Math.PI.toFloat() / 180f)

            val headRotation = Vector4d(rotateX.toDouble(), rotateY.toDouble(), 0.0, 1.0)
            val headTransform: Matrix4d = bone.calculateLocalTransform(
                bone.translation,
                headRotation, bone.scaling
            )
            val boneId: Int = skeleton.getBoneId(boneName)
            val parentBoneId: Int = skeleton.getBoneParentId(boneName)
            val parentTransform =
                if (parentBoneId != -1) Matrix4d(outPose.getJointMatrix(parentBoneId))
                else Matrix4d()
            outPose.setJointMatrix(boneId, parentTransform.mulAffine(headTransform))
        }
    }
}
