package ru.hollowhorizon.hc.common.events

import net.minecraft.resources.IResourcePack
import net.minecraftforge.eventbus.api.Event

class ResourcePackAddEvent(private val packs: ArrayList<IResourcePack>) : Event() {
    fun addPack(pack: IResourcePack) {
        packs.add(pack)
    }
}