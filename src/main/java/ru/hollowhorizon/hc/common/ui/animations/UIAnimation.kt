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
    var startTime = TickHandler.currentTicks
    val isEnd get() = trigger != AnimationTrigger.LOOP || TickHandler.currentTicks - startTime > duration

    fun reset() {
        startTime = TickHandler.currentTicks
    }

    fun update(partialTick: Float): Float {
        val time = (TickHandler.currentTicks - startTime + partialTick % duration) / duration

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