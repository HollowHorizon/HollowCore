package ru.hollowhorizon.hc

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import ru.hollowhorizon.hc.client.utils.currentServer
import ru.hollowhorizon.hc.common.events.EventBus.post
import ru.hollowhorizon.hc.common.events.entity.EntityTrackingEvent
import ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent

object ForgeEvents {
    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(::registerAttributes)
        MinecraftForge.EVENT_BUS.addListener(::registerReloadListeners)
        MinecraftForge.EVENT_BUS.addListener(::onServerStart)
        MinecraftForge.EVENT_BUS.addListener(::registerCommands)
        MinecraftForge.EVENT_BUS.addListener(::onServerTick)
        MinecraftForge.EVENT_BUS.addListener(::onEntityTracking)
        MinecraftForge.EVENT_BUS.addListener(::onPlayerJoin)
        MinecraftForge.EVENT_BUS.addListener(::onPlayerChangeDimension)
    }

    private fun registerAttributes(event: EntityAttributeCreationEvent) {
        val attributes = RegisterEntityAttributesEvent()
        post(attributes)
        attributes.getAttributes().forEach(event::put)
    }

    private fun registerReloadListeners(event: AddReloadListenerEvent) {
        val hcevent = RegisterReloadListenersEvent.Server()
        post(hcevent)
        hcevent.listeners.forEach(event::addListener)
    }


    private fun registerCommands(event: RegisterCommandsEvent) {
        ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent(
            event.dispatcher, event.buildContext, event.commandSelection
        ).post()
    }

    private fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Server(
                event.server
            )
        )
    }

    private fun onEntityTracking(event: net.minecraftforge.event.entity.player.PlayerEvent.StartTracking) {
        EntityTrackingEvent(event.entity, event.target).post()
    }

    private fun onPlayerJoin(event: net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent) {
        PlayerEvent.Join(event.entity).post()
    }

    private fun onPlayerChangeDimension(event: net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent) {
        val server = event.entity.server ?: return
        val from = server.getLevel(event.from) ?: return
        val to = server.getLevel(event.to) ?: return
        PlayerEvent.ChangeDimension(event.entity, from, to).post()
    }

    private fun onServerStart(event: ServerAboutToStartEvent) {
        currentServer = event.server
    }
}