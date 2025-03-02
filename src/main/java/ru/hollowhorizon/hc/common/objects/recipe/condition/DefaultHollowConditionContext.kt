package ru.hollowhorizon.hc.common.objects.recipe.condition

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagManager
import ru.hollowhorizon.hc.client.utils.JavaHacks
import java.util.Collections
import java.util.IdentityHashMap

class DefaultHollowConditionContext(private val tagManager: TagManager): HollowCondition.ConditionContext {
    private var loadedTags: Map<ResourceKey<*>, Map<ResourceLocation, Collection<Holder<*>>>>? = null

    override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
        if (loadedTags == null) {
            val tags = tagManager.result
            check(tags.isNotEmpty()) { "Tags have not been loaded yet." }

            loadedTags = IdentityHashMap()
            tags.forEach {
                val m = Collections.unmodifiableMap(it.tags)
                (loadedTags as IdentityHashMap)[it.key] = m
            }
        }

        return JavaHacks.forceCast(loadedTags!!.getOrDefault(registry, Collections.emptyMap()))
    }
}