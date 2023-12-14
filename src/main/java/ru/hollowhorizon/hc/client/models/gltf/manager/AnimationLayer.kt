package ru.hollowhorizon.hc.client.models.gltf.manager

import com.mojang.math.Quaternion
import kotlinx.serialization.Serializable
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationState
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode


@Serializable
data class AnimationLayer(
    val animation: String,
    val layerMode: LayerMode,
    val playMode: PlayMode,
    val speed: Float,
    var time: Int = 0,
    var state: AnimationState = AnimationState.STARTING,
    var fadeIn: Int = 10,
    var fadeOut: Int = 10,
) {
    val fadeInSeconds get() = fadeIn / 20f
    val fadeOutSeconds get() = fadeOut / 20f

    fun isEnd(
        nameToAnimationMap: Map<String, Animation>,
        currentTick: Int,
        partialTick: Float,
    ): Boolean {
        val animation = nameToAnimationMap[animation] ?: return true
        return (((currentTick - time + partialTick - fadeOut) / 20f) >= animation.maxTime) && state == AnimationState.FINISHED
    }

    fun computeTransform(
        node: GltfTree.Node,
        bindPose: Transformation,
        nameToAnimationMap: Map<String, Animation>,
        currentTick: Int,
        partialTick: Float,
    ): Transformation? {
        val animation = nameToAnimationMap[animation] ?: return null

        if (time == 0) time = currentTick
        val rawTime = (currentTick - time + partialTick) / 20f * speed

        val currentTime = when (playMode) {
            PlayMode.LOOPED -> rawTime % animation.maxTime
            PlayMode.LAST_FRAME -> rawTime.coerceAtMost(animation.maxTime)
            PlayMode.REVERSED -> {
                val isReversed = (rawTime / animation.maxTime).toInt() % 2 == 1
                if (!isReversed) rawTime % animation.maxTime
                else animation.maxTime - (rawTime % animation.maxTime)
            }

            PlayMode.ONCE -> {
                if (rawTime >= animation.maxTime) state = AnimationState.FINISHED
                rawTime
            }
        }

        return animation.compute(node, bindPose, currentTime)
//        return when (state) {
//            AnimationState.STARTING -> {
//                if (rawTime > fadeInSeconds) {
//                    state = AnimationState.PLAYING
//                }
//                Transformation.lerp(
//                    null,
//                    animation.compute(node, bindPose, currentTime),
//                    (currentTime / fadeInSeconds).coerceAtMost(1.0f)
//                )
//            }
//            AnimationState.PLAYING -> animation.compute(node, bindPose, currentTime)
//            AnimationState.FINISHED -> Transformation.lerp(
//                animation.compute(node, bindPose, animation.maxTime),
//                null,
//                (rawTime % fadeOutSeconds) / fadeOutSeconds
//            )
//        }
    }
}

class DefinedLayer {
    var currentAnimation = AnimationType.IDLE
    var currentStartTime = 0
    var lastAnimation = AnimationType.IDLE
    var lastStartTime = 0
    var priority = 0f

    fun update(animationType: AnimationType, currentTick: Int, partialTick: Float) {
        priority = ((currentTick - currentStartTime + partialTick) / 10f).coerceAtMost(1f)
        if (animationType == currentAnimation) return
        lastAnimation = currentAnimation
        currentAnimation = animationType

        //грубо говоря, как песочные часы, если перевернуть до полного перехода, то приоритет будет обратно пропорционален
        priority = 1f - priority
        lastStartTime = currentStartTime
        currentStartTime = currentTick
    }

    fun computeTransform(
        node: GltfTree.Node,
        bindPose: Transformation,
        animationCache: Map<AnimationType, Animation>,
        currentTick: Int,
        partialTick: Float,
    ): Transformation? {
        val f = animationCache[currentAnimation]
        val s = animationCache[lastAnimation]
        val firstTime = (currentTick - lastStartTime + partialTick) / 20 % (f?.maxTime ?: 0f)
        val secondTime = (currentTick - currentStartTime + partialTick) / 20 % (s?.maxTime ?: 0f)
        return Transformation.lerp(
            s?.compute(node, bindPose, secondTime),
            f?.compute(node, bindPose, firstTime),
            priority
        )
    }
}

class HeadLayer {
    fun computeRotation(
        animatable: LivingEntity,
        partialTick: Float,
    ): Quaternion {

        val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
        val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
        val netHeadYaw = headYaw - bodyYaw
        val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)

        return Quaternion(headPitch, netHeadYaw, 0f, true)

    }
}

enum class LayerMode {
    ADD, OVERWRITE
}