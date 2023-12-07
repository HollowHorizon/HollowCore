package ru.hollowhorizon.hc.client.models.gltf.manager

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree

object GltfManager {
    lateinit var lightTexture: AbstractTexture
    private val models = HashMap<ResourceLocation, GltfModel>()

    fun getOrCreate(location: ResourceLocation) = models.computeIfAbsent(location) { model ->
        GltfModel(GltfTree.parse(model))
    }

    @JvmStatic
    fun onReload(event: RegisterClientReloadListenersEvent) {
        lightTexture = Minecraft.getInstance().getTextureManager().getTexture(ResourceLocation("dynamic/light_map_1"))

        event.registerReloadListener(ResourceManagerReloadListener {
            models.clear()
        })
    }
}