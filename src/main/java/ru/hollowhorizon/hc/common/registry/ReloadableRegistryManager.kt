package ru.hollowhorizon.hc.common.registry

import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier

object ReloadableRegistryManager {
    val dummiesRegistries = hashMapOf<ResourceLocation, Supplier<*>>()

    @JvmStatic
    fun <V> getDummySupplier(registryName: ResourceLocation): Supplier<V> {
        return dummiesRegistries[registryName] as Supplier<V>
    }
}