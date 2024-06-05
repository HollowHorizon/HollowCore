package ru.hollowhorizon.hc

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterKeyBindingsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterShadersEvent

object ForgeClientEvents {
    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(::registerShaders)
        FMLJavaModLoadingContext.get().modEventBus.addListener(::onRegisterKeys)
        MinecraftForge.EVENT_BUS.addListener(::registerReloadListeners)
        MinecraftForge.EVENT_BUS.addListener(::onClientTick)
    }

    private fun registerReloadListeners(event: AddReloadListenerEvent) {
        val hcevent = RegisterClientReloadListenersEvent()
        EventBus.post(hcevent)
        hcevent.listeners.forEach {
            event.addListener(it)
        }
    }

    private fun registerShaders(event: net.minecraftforge.client.event.RegisterShadersEvent) {
        val hcEvent = RegisterShadersEvent()
        EventBus.post(hcEvent)
        hcEvent.shaders.forEach {
            event.registerShader(ShaderInstance(event.resourceProvider, it.key, it.value.first), it.value.second)
        }
    }

    private fun onClientTick(event: TickEvent.ClientTickEvent) {
        if(event.phase != TickEvent.Phase.END) return

        EventBus.post(
            ru.hollowhorizon.hc.common.events.tick.TickEvent.Client(
                Minecraft.getInstance()
            )
        )
    }

    private fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        RegisterKeyBindingsEvent(event::register).post()
    }
}