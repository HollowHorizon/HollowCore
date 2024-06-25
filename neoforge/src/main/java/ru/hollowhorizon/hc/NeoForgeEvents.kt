package ru.hollowhorizon.hc

import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import ru.hollowhorizon.hc.client.utils.currentServer
import ru.hollowhorizon.hc.common.events.EventBus.post
import ru.hollowhorizon.hc.common.events.entity.EntityTrackingEvent
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent
import ru.hollowhorizon.hc.internal.NeoForgeNetworkHelper

object NeoForgeEvents {
    init {
        HollowCoreNeoForge.MOD_BUS.addListener(::registerAttributes)
        HollowCoreNeoForge.MOD_BUS.addListener(NeoForgeNetworkHelper::register)
        NeoForge.EVENT_BUS.addListener(::registerReloadListeners)
        NeoForge.EVENT_BUS.addListener(::registerCommands)
        NeoForge.EVENT_BUS.addListener(::onServerTick)
        NeoForge.EVENT_BUS.addListener(::onEntityTracking)
        NeoForge.EVENT_BUS.addListener(::onPlayerJoin)
        NeoForge.EVENT_BUS.addListener(::onPlayerChangeDimension)
        NeoForge.EVENT_BUS.addListener(::onServerStart)
    }

    private fun registerReloadListeners(event: AddReloadListenerEvent) {
        val hcevent = RegisterReloadListenersEvent.Server()
        post(hcevent)
        hcevent.listeners.forEach(event::addListener)
    }

    private fun registerAttributes(event: EntityAttributeCreationEvent) {
        val attributes = RegisterEntityAttributesEvent()
        post(attributes)
        attributes.getAttributes().forEach(event::put)
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent(
            event.dispatcher, event.buildContext, event.commandSelection
        ).post()
    }

    private fun onServerTick(event: ServerTickEvent.Post) {
        post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Server(
                event.server
            )
        )
    }

    private fun onEntityTracking(event: PlayerEvent.StartTracking) {
        EntityTrackingEvent(event.entity, event.target).post()
    }

    private fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent.Join(event.entity).post()
    }

    private fun onPlayerChangeDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val server = event.entity.server ?: return
        val from = server.getLevel(event.from) ?: return
        val to = server.getLevel(event.to) ?: return
        ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent.ChangeDimension(event.entity, from, to).post()
    }

    private fun onServerStart(event: ServerAboutToStartEvent) {
        currentServer = event.server
    }
}