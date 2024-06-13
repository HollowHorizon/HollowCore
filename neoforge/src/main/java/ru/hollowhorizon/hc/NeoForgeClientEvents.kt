package ru.hollowhorizon.hc

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityRenderersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterKeyBindingsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterShadersEvent

object NeoForgeClientEvents {
    init {
        HollowCoreNeoForge.MOD_BUS.addListener(::registerShaders)
        HollowCoreNeoForge.MOD_BUS.addListener(::onRegisterKeys)
        HollowCoreNeoForge.MOD_BUS.addListener(::onEntityRenderers)
        HollowCoreNeoForge.MOD_BUS.addListener(::registerReloadListeners)
        NeoForge.EVENT_BUS.addListener(::onClientTick)
        NeoForge.EVENT_BUS.addListener(::onRenderTooltips)
    }

    private fun registerReloadListeners(event: net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent) {
        val hcevent = RegisterReloadListenersEvent()
        EventBus.post(hcevent)
        hcevent.listeners.forEach(event::registerReloadListener)
    }

    private fun registerShaders(event: net.neoforged.neoforge.client.event.RegisterShadersEvent) {
        val hcEvent = RegisterShadersEvent()
        EventBus.post(hcEvent)
        hcEvent.shaders.forEach {
            event.registerShader(ShaderInstance(event.resourceProvider, it.key, it.value.first), it.value.second)
        }
    }

    private fun onClientTick(event: ClientTickEvent.Post) {
        EventBus.post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Client(
                Minecraft.getInstance()
            )
        )
    }

    private fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        RegisterKeyBindingsEvent(event::register).post()
    }

    private fun onEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        RegisterEntityRenderersEvent(event::registerEntityRenderer).post()
    }

    private fun onRenderTooltips(event: ItemTooltipEvent) {
        ru.hollowhorizon.hc.common.events.client.ItemTooltipEvent(
            event.flags,
            event.itemStack,
            event.toolTip,
            event.context
        ).post()
    }
}