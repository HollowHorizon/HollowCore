package ru.hollowhorizon.hc.common.ui.animations

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler
import ru.hollowhorizon.hc.client.utils.math.Interpolation

@Serializable
class UIAnimation(
    vararg val targets: AnimationTarget,
    val trigger: AnimationTrigger,
    val duration: Int = 20,
    val startValue: Float = 0f,
    val endValue: Float = 1f,
    val interpolation: Interpolation = Interpolation.LINEAR,
) {
    @Transient
    var startTime = ClientTickHandler.clientTicks
    val isEnd get() = trigger != AnimationTrigger.LOOP || ClientTickHandler.clientTicks - startTime > duration

    fun reset() {
        startTime = ClientTickHandler.clientTicks
    }

    fun update(partialTick: Float): Float {
        val time = (ClientTickHandler.clientTicks - startTime + partialTick % duration) / duration

        return startValue + (endValue - startTime) * interpolation(time)
    }
}

enum class AnimationTarget {
    OFFSET_X, OFFSET_Y,
    SCALE_X, SCALE_Y,
    ROTATION,
    COLOR_R, COLOR_G, COLOR_B,
    TRANSPARENCY, CUSTOM;

    companion object
}

enum class AnimationTrigger {
    ON_OPEN, ON_CLOSE, ON_CLICK, ON_HOVER, ON_UNHOVER, LOOP, CUSTOM;
}