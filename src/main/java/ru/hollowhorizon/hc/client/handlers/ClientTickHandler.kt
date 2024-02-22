package ru.hollowhorizon.hc.client.handlers

import net.minecraft.client.Minecraft
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import ru.hollowhorizon.hc.client.utils.isLogicalClient

object ClientTickHandler {
    @JvmField
    var ticksNotPaused = 0
    @JvmField
    var clientTicks = 0
    @JvmField
    var serverTicks = 0

    @JvmStatic
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

    fun currentTicks() = if(isLogicalClient) ticksNotPaused else serverTicks
}
