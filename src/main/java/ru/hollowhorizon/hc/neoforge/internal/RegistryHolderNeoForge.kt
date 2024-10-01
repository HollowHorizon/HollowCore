//? if neoforge {
/*package ru.hollowhorizon.hc.neoforge.internal

import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.stats.StatType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.entity.schedule.Schedule
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.levelgen.carver.WorldCarver
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import ru.hollowhorizon.hc.neoforge.HollowCoreNeoForge
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.BlockItemProperties
import ru.hollowhorizon.hc.common.registry.AutoModelType
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderNeoForge<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: AutoModelType?,
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

            Fluid::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.FLUID,
                location.namespace
            )

            CreativeModeTab::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.CREATIVE_MODE_TAB, location.namespace
            )

            ParticleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.PARTICLE_TYPE,
                location.namespace
            )
            DataComponentType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.DATA_COMPONENT_TYPE.key(), location.namespace
            )

            MobEffect::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.MOB_EFFECT,
                location.namespace
            )

            Potion::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.POTION,
                location.namespace
            )

            RecipeType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.RECIPE_TYPE,
                location.namespace
            )

            Attribute::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.ATTRIBUTE,
                location.namespace
            )

            StatType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.STAT_TYPE,
                location.namespace
            )

            ArgumentTypeInfo::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.COMMAND_ARGUMENT_TYPE,
                location.namespace
            )

            VillagerProfession::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.VILLAGER_PROFESSION,
                location.namespace
            )

            PoiType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE,
                location.namespace
            )

            MemoryModuleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.MEMORY_MODULE_TYPE,
                location.namespace
            )

            SensorType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.SENSOR_TYPE,
                location.namespace
            )

            Schedule::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.SCHEDULE,
                location.namespace
            )

            Activity::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.ACTIVITY,
                location.namespace
            )

            WorldCarver::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.CARVER,
                location.namespace
            )

            ChunkStatus::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.CHUNK_STATUS,
                location.namespace
            )

            BlockStateProviderType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE,
                location.namespace
            )

            FoliagePlacerType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.FOLIAGE_PLACER_TYPE,
                location.namespace
            )

            TreeDecoratorType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.TREE_DECORATOR_TYPE,
                location.namespace
            )

            registry != null -> DeferredRegister.create(registry, location.namespace)

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as DeferredRegister<T>

    private val result: DeferredHolder<T, T> = registryType.register(location.path, supplier).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addBlockModel(location, autoModel)

                if (BlockItemProperties::class.java.isAssignableFrom(target)) {
                    val items: DeferredRegister<Item> = DeferredRegister.createItems(location.namespace)
                    items.register(
                        location.path
                    ) { _ ->
                        BlockItem(this as Block, (this as BlockItemProperties).properties)
                    }
                    items.register(HollowCoreNeoForge.MOD_BUS)
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addItemModel(location, autoModel)
            }
        }
        registryType.register(HollowCoreNeoForge.MOD_BUS)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result.get() }
    }
}
*///?}