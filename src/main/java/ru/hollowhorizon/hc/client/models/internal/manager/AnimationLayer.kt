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

package ru.hollowhorizon.hc.client.models.internal.manager

import de.fabmax.kool.math.MutableQuatF
import de.fabmax.kool.math.QuatF
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.deg
import de.fabmax.kool.scene.TrsTransformF
import de.fabmax.kool.util.Time
import kotlinx.serialization.Serializable
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.internal.Node
import ru.hollowhorizon.hc.client.models.internal.animations.Animation
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationState
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationType
import ru.hollowhorizon.hc.client.models.internal.animations.PlayMode
import kotlin.math.abs


@Serializable
data class AnimationLayer(
    val animation: String,
    val layerMode: LayerMode,
    val playMode: PlayMode,
    val speed: Float,
    var state: AnimationState = AnimationState.STARTING,
    var fadeIn: Int = 10,
    var fadeOut: Int = 10,
) {
    private val fadeInSeconds get() = fadeIn / 20f
    private val fadeOutSeconds get() = fadeOut / 20f

    private var currentTime = 0f
    private var finishTime = 0f

    fun reset() {
        currentTime = 0f
        finishTime = 0f
    }

    fun update() {
        currentTime += Time.deltaT
    }

    fun isEnd(): Boolean {
        if (state == AnimationState.FINISHED) {
            if (finishTime == 0f) finishTime = currentTime
            return currentTime - finishTime >= fadeOutSeconds
        }
        return false
    }

    fun computeTransform(
        node: Node,
        nameToAnimationMap: Map<String, Animation>,
        baseTransform: TrsTransformF?,
    ): TrsTransformF? {
        val animation = nameToAnimationMap[animation] ?: return null

        val rawTime = currentTime * speed

        val currentTime = when (playMode) {
            PlayMode.LOOPED -> rawTime % animation.duration
            PlayMode.LAST_FRAME -> rawTime.coerceAtMost(animation.duration)
            PlayMode.REVERSED -> {
                val isReversed = (rawTime / animation.duration).toInt() % 2 == 1
                if (!isReversed) rawTime % animation.duration
                else animation.duration - (rawTime % animation.duration)
            }

            PlayMode.ONCE -> {
                if (rawTime >= animation.duration) state = AnimationState.FINISHED
                rawTime
            }
        }

        return null
//        return when (state) {
//            AnimationState.STARTING -> {
//                if (rawTime > fadeInSeconds) {
//                    state = AnimationState.PLAYING
//                }
//                TrsTransformF().lerp(
//                    baseTransform,
//                    animation.compute(node, rawTime),
//                    (rawTime / fadeInSeconds).coerceAtMost(1.0f)
//                )
//            }
//
//            AnimationState.PLAYING -> animation.compute(node, currentTime)
//            AnimationState.FINISHED -> {
//                if (finishTime == 0f) finishTime = currentTime
//                Transformation.lerp(
//                    animation.compute(node, currentTime),
//                    baseTransform,
//                    (currentTime - finishTime) / fadeOutSeconds
//                )
//            }
//        }
    }
}

class DefinedLayer {
    private var current = AnimationType.IDLE
    private var last = AnimationType.IDLE

    private var currentElapsed = 0f
    private var lastElapsed = 0f
    private var transitionElapsed = 0f

    companion object {
        const val TRANSITION_FACTOR = 0.25f
    }

    fun update(next: AnimationType, speed: Float) {
        val dtAnim = Time.deltaT * 1f
        currentElapsed += dtAnim
        lastElapsed += dtAnim
        transitionElapsed += Time.deltaT

        // если анимация не сменилась — только обновляем таймер перехода
        if (next == current) return

        // начало перехода в новую
        last = current
        current = next

        // сбрасываем
        lastElapsed = currentElapsed    // старая анимация начинается с того же момента, что и новая
        currentElapsed = 0f
        transitionElapsed = 0f
    }

    fun computeTransform(
        node: Node,
        animations: Map<AnimationType, Animation>,
    ): TrsTransformF? {
        return null
//        val f = animations[current] ?: return null
//        val s = animations[last]    ?: return f.compute(node, wrap(currentElapsed, f.maxTime, current))
//
//        val t = (transitionElapsed / TRANSITION_FACTOR).coerceIn(0f, 1f)
//
//        val timeCur  = wrap(currentElapsed,   f.maxTime, current)
//        val timeLast = wrap(lastElapsed,      s.maxTime, last)
//
//        val poseCur  = f.compute(node, timeCur)
//        val poseLast = s.compute(node, timeLast)
//
//        return Transformation.lerp(poseLast, poseCur, t)
    }

    private fun wrap(time: Float, max: Float, type: AnimationType): Float {
        if (max <= 0f) return 0f
        var r = time % max
        if (r < 0f) r += max
        return r
    }
}

object HeadLayer {
    fun computeRotation(
        animatable: LivingEntity,
        switchHeadRot: Boolean,
        partialTick: Float,
    ): QuatF {

        val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
        val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
        val netHeadYaw = headYaw - bodyYaw
        val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)

        val xRot: QuatF
        val yRot: QuatF

        if (switchHeadRot) {
            xRot = MutableQuatF().rotate(headPitch.deg, Vec3f.Y_AXIS)
            yRot = MutableQuatF().rotate(netHeadYaw.deg, Vec3f.X_AXIS)
        } else {
            xRot = MutableQuatF().rotate(headPitch.deg, Vec3f.X_AXIS)
            yRot = MutableQuatF().rotate(netHeadYaw.deg, Vec3f.Y_AXIS)
        }

        return yRot.mul(xRot)

    }
}

enum class LayerMode {
    ADD, OVERWRITE
}