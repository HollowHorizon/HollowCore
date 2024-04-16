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

package ru.hollowhorizon.hc.client.handlers

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import ru.hollowhorizon.hc.client.utils.isLogicalClient
import kotlin.time.Duration

object TickHandler {
    @JvmField
    var ticksNotPaused = 0

    @JvmField
    var clientTicks = 0

    @JvmField
    var serverTicks = 0

    val lazyCurrentTicks get() = lazy { currentTicks() }
    val currentTicks get() = currentTicks()
    val partialTicks get() = Minecraft.getInstance().partialTick

    fun computeTime(startTime: Int, duration: Int): Float = (currentTicks - startTime + partialTicks) / duration

    @JvmStatic
    @OnlyIn(Dist.CLIENT)
    fun clientTickEnd(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            if (!Minecraft.getInstance().isPaused) ticksNotPaused++
            clientTicks++
        }
    }

    @JvmStatic
    fun serverTickEnd(event: ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) serverTicks++
    }

    fun currentTicks() = if (isLogicalClient) clientTicks else serverTicks
}
