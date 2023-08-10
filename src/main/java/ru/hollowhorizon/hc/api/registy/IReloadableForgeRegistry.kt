package ru.hollowhorizon.hc.api.registy

import net.minecraft.resources.ResourceLocation


interface IReloadableForgeRegistry<V> {
    fun registerEntry(name: ResourceLocation, registryEntry: V): V
    fun removeEntry(name: ResourceLocation)
    fun onReload()
}