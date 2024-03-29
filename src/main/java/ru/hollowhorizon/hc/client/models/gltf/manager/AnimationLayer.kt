package ru.hollowhorizon.hc.client.models.gltf.manager

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
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
    private var finishTicks = 0
    private val fadeInSeconds get() = fadeIn / 20f
    private val fadeOutSeconds get() = fadeOut / 20f

    fun isEnd(
        currentTick: Int,
        partialTick: Float,
    ): Boolean {
        if (state == AnimationState.FINISHED) {
            if (finishTicks == 0) finishTicks = currentTick

            val currentTime = (currentTick - finishTicks + partialTick) / 20f

            return currentTime >= fadeOutSeconds
        }
        return false
    }

    fun computeTransform(
        node: GltfTree.Node,
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

        return when (state) {
            AnimationState.STARTING -> {
                if (rawTime > fadeInSeconds) {
                    state = AnimationState.PLAYING
                }
                Transformation.lerp(
                    null,
                    animation.compute(node, rawTime),
                    (rawTime / fadeInSeconds).coerceAtMost(1.0f)
                )
            }

            AnimationState.PLAYING -> animation.compute(node, currentTime)
            AnimationState.FINISHED -> {
                if (finishTicks == 0) finishTicks = currentTick
                Transformation.lerp(
                    animation.compute(node, currentTime),
                    null,
                    (currentTick - finishTicks + partialTick) / 20f / fadeOutSeconds
                )
            }
        }
    }
}

class DefinedLayer {
    private var currentAnimation = AnimationType.IDLE
    private var lastAnimation = AnimationType.IDLE
    private var currentStartTime = 0
    private var priority = 0f

    fun update(animationType: AnimationType, currentSpeed: Float, currentTick: Int, partialTick: Float) {
        priority = ((currentTick - currentStartTime + partialTick) / 10f * currentSpeed).coerceAtMost(1f)
        if (animationType == currentAnimation) return
        lastAnimation = currentAnimation
        currentAnimation = animationType

        //грубо говоря, как песочные часы, если перевернуть до полного перехода, то приоритет будет обратно пропорционален
        priority = 1f - priority
        currentStartTime = currentTick
    }

    fun computeTransform(
        node: GltfTree.Node,
        animationCache: Map<AnimationType, Animation>,
        currentSpeed: Float,
        currentTick: Int,
        partialTick: Float,
    ): Transformation? {
        val f = animationCache[currentAnimation]
        val s = animationCache[lastAnimation]

        val speed = if(currentAnimation.hasSpeed) currentSpeed else 1.0f

        val time = (currentTick + partialTick) / 20 * speed

        val firstTime = time % (f?.maxTime ?: 0f)
        val secondTime = time % (s?.maxTime ?: 0f)

        return Transformation.lerp(
            s?.compute(node, secondTime),
            f?.compute(node, firstTime),
            priority
        )
    }
}

class HeadLayer {
    fun computeRotation(
        animatable: LivingEntity,
        switchHeadRot: Boolean,
        partialTick: Float,
    ): Quaternion {

        val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
        val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
        val netHeadYaw = headYaw - bodyYaw
        val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)

        val xRot: Quaternion
        val yRot: Quaternion

        if(switchHeadRot) {
            xRot = Vector3f.YP.rotationDegrees(headPitch)
            yRot = Vector3f.XP.rotationDegrees(netHeadYaw)
        } else {
            xRot = Vector3f.XP.rotationDegrees(headPitch)
            yRot = Vector3f.YP.rotationDegrees(netHeadYaw)
        }

        yRot.mul(xRot)

        return yRot

    }
}

enum class LayerMode {
    ADD, OVERWRITE
}