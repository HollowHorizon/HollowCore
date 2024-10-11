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
import ru.hollowhorizon.hc.client.utils.isLogicalClient
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.tick.TickEvent

object TickHandler {
    private var clientTicks = 0
    private var serverTicks = 0

    val currentTicks get() = if (isLogicalClient) clientTicks else serverTicks
    val partialTick: Float
        get() {
            //? if <1.21 {
            return Minecraft.getInstance().deltaFrameTime
            //?} elif >=1.21 {
            /*return Minecraft.getInstance().timer.realtimeDeltaTicks
            *///?}
        }
    val tickRate: Float
        get() {
            //? if <1.21 {
            return 20f
            //?} elif >=1.21 {
            /*return (Minecraft.getInstance().level?.tickRateManager()?.tickrate()?.coerceAtLeast(20f)
                ?: 20f) // В deltaFrame уже учтены значения ниже 20
            *///?}
        }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.Client) {
        clientTicks++
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.Server) {
        serverTicks++
    }
}
