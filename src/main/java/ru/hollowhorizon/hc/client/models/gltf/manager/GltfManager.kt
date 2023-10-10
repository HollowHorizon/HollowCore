package ru.hollowhorizon.hc.client.models.gltf.manager

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree

object GltfManager {
    private val models = HashMap<ResourceLocation, GltfModel>()

    fun getOrCreate(location: ResourceLocation) = models.computeIfAbsent(location) { model ->
        GltfModel(GltfTree.parse(model))
    }

    @JvmStatic
    fun onReload(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(ResourceManagerReloadListener {
            models.clear()
        })
    }
}