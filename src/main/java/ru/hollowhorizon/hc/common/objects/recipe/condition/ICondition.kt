package ru.hollowhorizon.hc.common.objects.recipe.condition

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

interface ICondition {
    val id: ResourceLocation

    fun test(context: ConditionContext)

    interface ConditionContext {
        companion object {
            @JvmField
            val EMPTY = object : ConditionContext {
                override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> =
                    mapOf()
            }

            @JvmField
            val TAGS_INVALID = object : ConditionContext {
                override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> =
                    throw UnsupportedOperationException("Usage of tag-based conditions is not permitted in this context!")
            }
        }

        fun <T> getTag(key: TagKey<T>): Collection<Holder<T>> {
            return getAllTags(key.registry()).getOrDefault(key.location(), setOf<Holder<T>>())
        }

        fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>>
    }
}