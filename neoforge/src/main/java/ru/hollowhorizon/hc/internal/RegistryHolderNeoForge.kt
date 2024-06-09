package ru.hollowhorizon.hc.internal

import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.feature.Feature
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import ru.hollowhorizon.hc.HollowCoreNeoForge
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderNeoForge<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: Boolean,
    supplier: () -> T,
    val target: Class<T>,
) :
    IRegistryHolder<T> {
    val registryType: DeferredRegister<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> DeferredRegister.createBlocks(location.namespace)
            Item::class.java.isAssignableFrom(this) -> DeferredRegister.createItems(location.namespace)
            EntityType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.ENTITY_TYPE,
                location.namespace
            )

            BlockEntityType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                location.namespace
            )

            SoundEvent::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.SOUND_EVENT,
                location.namespace
            )

            Feature::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.FEATURE,
                location.namespace
            )

            RecipeSerializer::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.RECIPE_SERIALIZER,
                location.namespace
            )

            MenuType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.MENU,
                location.namespace
            )

            ParticleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.PARTICLE_TYPE,
                location.namespace
            )

            ResourceKey::class.java.isAssignableFrom(this) -> DeferredRegister.create(registry ?: throw IllegalStateException("Registry is null!"), location.namespace)

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as DeferredRegister<T>

    private val result: DeferredHolder<T, T> = registryType.register(location.path, supplier).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.genBlockData.add(location)

                if (IBlockItemProperties::class.java.isAssignableFrom(target)) {
                    val items: DeferredRegister<Item> = DeferredRegister.createItems(location.namespace)
                    items.register(
                        location.path
                    ) { _ ->
                        BlockItem(this as Block, (this as IBlockItemProperties).properties)
                    }
                    items.register(HollowCoreNeoForge.MOD_BUS)
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.genItemModels.add(location)
            }
        }
        registryType.register(HollowCoreNeoForge.MOD_BUS)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result.get() }
    }
}