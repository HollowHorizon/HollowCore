package ru.hollowhorizon.hc.particles.client.registry

import ru.hollowhorizon.hc.particles.client.loader.EffekAssetLoader.Companion.get
import net.minecraft.resources.ResourceLocation
import java.util.function.BiConsumer


object EffectRegistry {
    @JvmStatic
    fun get(id: ResourceLocation): EffectDefinition? {
        return get().get(id)
    }

    fun entries(): Collection<Map.Entry<ResourceLocation, EffectDefinition>> {
        return get().entries()
    }

    fun forEach(action: BiConsumer<ResourceLocation, EffectDefinition>) {
        get().forEach(action)
    }
}
