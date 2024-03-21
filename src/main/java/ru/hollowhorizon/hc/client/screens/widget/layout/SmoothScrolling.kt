package ru.hollowhorizon.hc.client.screens.widget.layout

import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.utils.math.Interpolation

class SmoothScrolling(private var duration: Int = 10, val startValue: Float = 0f, val endValue: Float = 1f, val interpolation: Interpolation = Interpolation.LINEAR, val action: (Float) -> Unit) {
    private val startTime by TickHandler.lazyCurrentTicks

    fun update(): Boolean {
        val time = TickHandler.computeTime(startTime, duration)
        if (time >= 1f) {
            action(endValue)
            return true
        }
        action(interpolation(time) * (endValue - startValue) + startValue)
        return false
    }

}