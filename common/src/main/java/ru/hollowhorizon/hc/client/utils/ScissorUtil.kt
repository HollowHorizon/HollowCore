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

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

object ScissorUtil {
    private val stack = ArrayDeque<Frame>()

    fun push(x: Int, y: Int, width: Int, height: Int) =
        push(Frame(x, y, x + width, y + height))

    fun push(frame: Frame) {
        stack.addLast(frame)
        apply()
    }

    fun pop(): Frame {
        val frame = stack.removeLast()
        apply()
        return frame
    }

    @OptIn(ExperimentalContracts::class)
    inline fun suspendScissors(fn: () -> Unit) {
        contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
        val frame = pop()
        fn()
        push(frame)
    }

    private fun apply() {
        val window = Minecraft.getInstance().window

        if (stack.isEmpty()) {
            RenderSystem.disableScissor()
            return
        }

        var x1 = 0
        var y1 = 0
        var x2 = window.guiScaledWidth
        var y2 = window.guiScaledHeight

        for (frame in stack) {
            x1 = max(x1, frame.x1)
            y1 = max(y1, frame.y1)
            x2 = min(x2, frame.x2)
            y2 = min(y2, frame.y2)
        }

        val scale = window.guiScale
        RenderSystem.enableScissor(
            (x1 * scale).toInt(), (window.height - scale * y2).toInt(),
            ((x2 - x1) * scale).toInt(), ((y2 - y1) * scale).toInt()
        )
    }

    class Frame(val x1: Int, val y1: Int, val x2: Int, val y2: Int)
}
