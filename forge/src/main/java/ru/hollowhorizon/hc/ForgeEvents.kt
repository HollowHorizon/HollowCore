package ru.hollowhorizon.hc

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.EventBus.post
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent

object ForgeEvents {
    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(::registerAttributes)
        MinecraftForge.EVENT_BUS.addListener(::registerCommands)
        MinecraftForge.EVENT_BUS.addListener(::onServerTick)
    }

    private fun registerAttributes(event: EntityAttributeCreationEvent) {
        val attributes = RegisterEntityAttributesEvent()
        EventBus.post(attributes)
        attributes.getAttributes().forEach(event::put)
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent(
            event.dispatcher, event.buildContext, event.commandSelection
        ).post()
    }

    private fun onServerTick(event: TickEvent.ServerTickEvent) {
        if(event.phase != TickEvent.Phase.END) return
        post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Server(
                event.server
            )
        )
    }
}