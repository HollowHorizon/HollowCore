/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.gltf.manager

import kotlinx.serialization.Serializable
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationState
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.utils.Axis
import ru.hollowhorizon.hc.client.utils.rotate


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

        val speed = if (currentAnimation.hasSpeed) currentSpeed else 1.0f

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
    ): Quaternionf {

        val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
        val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
        val netHeadYaw = headYaw - bodyYaw
        val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)

        val xRot: Quaternionf
        val yRot: Quaternionf

        if (switchHeadRot) {
            xRot = Quaternionf().rotateY(headPitch * Mth.DEG_TO_RAD)
            yRot = Quaternionf().rotateX(netHeadYaw * Mth.DEG_TO_RAD)
        } else {
            xRot = Quaternionf().rotateX(headPitch * Mth.DEG_TO_RAD)
            yRot = Quaternionf().rotateY(netHeadYaw * Mth.DEG_TO_RAD)
        }

        return yRot.mul(xRot)

    }
}

enum class LayerMode {
    ADD, OVERWRITE
}