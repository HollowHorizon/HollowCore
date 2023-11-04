package ru.hollowhorizon.hc.client.handlers

import net.minecraft.client.Minecraft
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent

object ClientTickHandler {
    @JvmField
    var ticksNotPaused = 0
    @JvmField
    var ticks = 0

    @JvmStatic
    fun clientTickEnd(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            if (!Minecraft.getInstance().isPaused) ticksNotPaused++
            ticks++
        }
    }
}
