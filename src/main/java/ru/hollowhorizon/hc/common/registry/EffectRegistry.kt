package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.client.render.effekseer.loader.EffekAssetLoader.Companion.get
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.render.effekseer.EffectDefinition
import java.util.function.BiConsumer


object EffectRegistry {
    @JvmStatic
    fun get(id: ResourceLocation): EffectDefinition? = get().get(id)

    fun entries(): Collection<Map.Entry<ResourceLocation, EffectDefinition>> = get().entries()

    fun forEach(action: BiConsumer<ResourceLocation, EffectDefinition>) {
        get().forEach(action)
    }
}
