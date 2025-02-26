//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import ru.hollowhorizon.hc.client.utils.currentServer
import ru.hollowhorizon.hc.common.events.EventBus.post
import ru.hollowhorizon.hc.common.events.entity.EntityTrackingEvent
import ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hc.common.events.item.BuildTabContentsEvent
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent
import ru.hollowhorizon.hc.common.events.server.ServerEvent

object ForgeEvents {
    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeEvents::registerAttributes)
        FMLJavaModLoadingContext.get().modEventBus.addListener(ForgeEvents::onBuildCreativeTab)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::registerReloadListeners)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onServerStart)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onServerStop)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::registerCommands)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onServerTick)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onEntityTracking)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onPlayerJoin)
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onPlayerChangeDimension)
        MinecraftForge.EVENT_BUS.addListener(::onBlockBreak)
    }

    private fun onBuildCreativeTab(event: BuildCreativeModeTabContentsEvent) {
        val buildEvent = BuildTabContentsEvent(event.tab, event.tabKey, event.parameters, event::accept)
        buildEvent.post()
    }

    private fun onBlockBreak(event: BlockEvent.BreakEvent) {
        val breakEvent = ru.hollowhorizon.hc.common.events.blocks.BlockEvent.Break(event.player.level(), event.pos, event.state, event.player)
        breakEvent.post()
        event.isCanceled = breakEvent.isCanceled
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
        ServerEvent.Starting(currentServer).post()
    }

    private fun onServerStop(event: ServerStoppingEvent) {
        ServerEvent.Stoping(event.server).post()
    }
}
*///?}