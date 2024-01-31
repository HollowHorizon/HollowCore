package ru.hollowhorizon.hc.common.registry

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.RegisterShadersEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import ru.hollowhorizon.hc.HollowCore.MODID

object ModShaders {
    lateinit var GLTF_ENTITY: ShaderInstance
    lateinit var GLTF_ENTITY_COLOR_PICK: ShaderInstance

    @SubscribeEvent
    fun onShaderRegistry(event: RegisterShadersEvent) {
        GLTF_ENTITY = ShaderInstance(
            event.resourceManager,
            ResourceLocation(MODID, "gltf_entity"),
            DefaultVertexFormat.NEW_ENTITY
        )
        event.registerShader(GLTF_ENTITY) {}

        GLTF_ENTITY_COLOR_PICK = ShaderInstance(
            event.resourceManager,
            ResourceLocation(MODID, "gltf_entity_color_pick"),
            DefaultVertexFormat.NEW_ENTITY
        )
        event.registerShader(GLTF_ENTITY_COLOR_PICK) {}
    }
}