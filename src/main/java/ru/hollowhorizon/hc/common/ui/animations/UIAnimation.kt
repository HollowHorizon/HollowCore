package ru.hollowhorizon.hc.common.ui.animations

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
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
    var startTime = TickHandler.clientTicks
    val isEnd get() = trigger != AnimationTrigger.LOOP || TickHandler.clientTicks - startTime > duration

    fun reset() {
        startTime = TickHandler.clientTicks
    }

    fun update(partialTick: Float): Float {
        val time = (TickHandler.clientTicks - startTime + partialTick % duration) / duration

        return startValue + (endValue - startTime) * interpolation(time)
    }

    fun start(widget: HollowWidget) {

    }

    fun loop(widget: HollowWidget) {

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