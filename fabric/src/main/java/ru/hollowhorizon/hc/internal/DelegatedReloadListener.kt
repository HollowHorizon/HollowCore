package ru.hollowhorizon.hc.internal

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener

class DelegatedReloadListener(private val eventListener: PreparableReloadListener): PreparableReloadListener by eventListener, IdentifiableResourceReloadListener {
    override fun getFabricId(): ResourceLocation {
        return ResourceLocation("hollowcore_generated", eventListener.javaClass.name.lowercase().replace("$", "."))
    }
}