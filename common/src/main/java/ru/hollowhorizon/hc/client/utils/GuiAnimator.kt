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

package ru.hollowhorizon.hc.client.utils

import ru.hollowhorizon.hc.client.handlers.TickHandler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class GuiAnimator protected constructor(
    val begin: Int,
    val end: Int,
    val maxTime: Int,
    protected val interpolation: (Float) -> Float,
) : ReadOnlyProperty<Any?, Int> {
    var value: Float = begin.toFloat()
    private var startTicks = TickHandler.currentTicks

    open fun update(partialTick: Float) {
        if (isFinished()) return

        val currentTime = TickHandler.currentTicks - startTicks + partialTick
        value = begin + (end - begin) * interpolation(currentTime / maxTime)
    }

    fun isFinished(): Boolean {
        return TickHandler.currentTicks - startTicks > maxTime
    }

    fun reset() {
        startTicks = TickHandler.currentTicks
        value = begin.toFloat()
    }

    class Reversed(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        private var switch = false

        override fun update(partialTick: Float) {
            super.update(partialTick)
            if (switch) value = end - value

            if (isFinished()) {
                switch = !switch
                reset()
            }
        }
    }

    class Looped(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation) {
        override fun update(partialTick: Float) {
            super.update(partialTick)
            if (isFinished()) reset()
        }
    }

    class Single(begin: Int, end: Int, time: Int, interpolation: (Float) -> Float) :
        GuiAnimator(begin, end, time, interpolation)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        update(TickHandler.partialTick)
        return value.toInt()
    }
}