package ru.hollowhorizon.hc

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.server.packs.PackType
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityRenderersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterShadersEvent
import ru.hollowhorizon.hc.internal.DelegatedReloadListener

object FabricClientEvents {
    init {
        registerShaders()
        registerReloadListeners()
        RegisterEntityRenderersEvent { entity, renderer ->
            EntityRenderers.register(entity, renderer)
        }.post()
    }

    private fun registerReloadListeners() {
        val event = RegisterClientReloadListenersEvent()
        EventBus.post(event)
        val helper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
        event.listeners.forEach {
            helper.registerReloadListener(DelegatedReloadListener(it))
        }
    }

    private fun registerShaders() {
        CoreShaderRegistrationCallback.EVENT.register(
            CoreShaderRegistrationCallback { ctx: CoreShaderRegistrationCallback.RegistrationContext ->
                val event = RegisterShadersEvent()
                EventBus.post(event)
                event.shaders.forEach {
                    ctx.register(it.key, it.value.first, it.value.second)
                }
            }
        )
    }
}