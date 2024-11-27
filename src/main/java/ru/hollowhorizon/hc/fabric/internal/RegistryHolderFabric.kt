//? if fabric {
package ru.hollowhorizon.hc.fabric.internal

import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.stats.StatType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.npc.VillagerProfession
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
import net.minecraft.world.level.chunk.ChunkStatus
import net.minecraft.world.level.levelgen.carver.WorldCarver
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.material.Fluid
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.BlockItemProperties
import ru.hollowhorizon.hc.common.registry.AutoModelType
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderFabric<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: AutoModelType?,
    supplier: () -> T,
    val target: Class<T>,
) :
    IRegistryHolder<T> {
    val registryType: Registry<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK
            Item::class.java.isAssignableFrom(this) -> BuiltInRegistries.ITEM
            EntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.ENTITY_TYPE
            BlockEntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK_ENTITY_TYPE
            SoundEvent::class.java.isAssignableFrom(this) -> BuiltInRegistries.SOUND_EVENT
            Feature::class.java.isAssignableFrom(this) -> BuiltInRegistries.FEATURE
            RecipeSerializer::class.java.isAssignableFrom(this) -> BuiltInRegistries.RECIPE_SERIALIZER
            CreativeModeTab::class.java.isAssignableFrom(this) -> BuiltInRegistries.CREATIVE_MODE_TAB
            Fluid::class.java.isAssignableFrom(this) -> BuiltInRegistries.FLUID
            MenuType::class.java.isAssignableFrom(this) -> BuiltInRegistries.MENU
            ParticleType::class.java.isAssignableFrom(this) -> BuiltInRegistries.PARTICLE_TYPE
            MobEffect::class.java.isAssignableFrom(this) -> BuiltInRegistries.MOB_EFFECT
            Potion::class.java.isAssignableFrom(this) -> BuiltInRegistries.POTION
            RecipeType::class.java.isAssignableFrom(this) -> BuiltInRegistries.RECIPE_TYPE
            Attribute::class.java.isAssignableFrom(this) -> BuiltInRegistries.ATTRIBUTE
            StatType::class.java.isAssignableFrom(this) -> BuiltInRegistries.STAT_TYPE
            ArgumentTypeInfo::class.java.isAssignableFrom(this) -> BuiltInRegistries.COMMAND_ARGUMENT_TYPE
            VillagerProfession::class.java.isAssignableFrom(this) -> BuiltInRegistries.VILLAGER_PROFESSION
            PoiType::class.java.isAssignableFrom(this) -> BuiltInRegistries.POINT_OF_INTEREST_TYPE
            MemoryModuleType::class.java.isAssignableFrom(this) -> BuiltInRegistries.MEMORY_MODULE_TYPE
            Schedule::class.java.isAssignableFrom(this) -> BuiltInRegistries.SCHEDULE
            WorldCarver::class.java.isAssignableFrom(this) -> BuiltInRegistries.CARVER
            ChunkStatus::class.java.isAssignableFrom(this) -> BuiltInRegistries.CHUNK_STATUS
            BlockStateProviderType::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE
            FoliagePlacerType::class.java.isAssignableFrom(this) -> BuiltInRegistries.FOLIAGE_PLACER_TYPE
            TreeDecoratorType::class.java.isAssignableFrom(this) -> BuiltInRegistries.TREE_DECORATOR_TYPE

            registry != null -> registry

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as Registry<T>

    private val result: T = Registry.register(registryType, location, supplier()).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addBlockModel(location, autoModel)

                if (BlockItemProperties::class.java.isAssignableFrom(target)) {
                    Registry.register(
                        BuiltInRegistries.ITEM,
                        location,
                        BlockItem(this as Block, (this as BlockItemProperties).properties)
                    )
                    if (autoModel != null) {
                        if (autoModel == AutoModelType.CUBE_ALL) HollowPack.addItemModel(
                            location,
                            AutoModelType.custom("${location.namespace}:block/${location.path}")
                        )
                        else HollowPack.addItemModel(location, AutoModelType.custom("builtin/entity"))
                    }
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addItemModel(location, autoModel)
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result }
    }
}
//?}