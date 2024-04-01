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
