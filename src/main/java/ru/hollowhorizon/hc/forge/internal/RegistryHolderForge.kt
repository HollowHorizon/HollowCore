package ru.hollowhorizon.hc.forge.internal

//? if forge {

/*//? if >=1.21 {
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.chunk.status.ChunkStatus
//?} elif >=1.20.1 {
/^import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.chunk.ChunkStatus
^///?} else {
/^import net.minecraft.world.level.chunk.ChunkStatus
^///?}
import net.minecraft.commands.synchronization.ArgumentTypeInfo

import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.stats.StatType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.decoration.PaintingVariant
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

import net.minecraft.world.level.levelgen.carver.WorldCarver
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import ru.hollowhorizon.hc.common.registry.AutoModelType
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderForge<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: AutoModelType?,
    supplier: () -> T,
    val target: Class<T>,
) :
    IRegistryHolder<T> {
    val registryType: DeferredRegister<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.BLOCKS,
                location.namespace
            )

            Item::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.ITEMS,
                location.namespace
            )

            EntityType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.ENTITY_TYPES,
                location.namespace
            )

            BlockEntityType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.BLOCK_ENTITY_TYPES,
                location.namespace
            )

            SoundEvent::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.SOUND_EVENTS,
                location.namespace
            )

            Feature::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.FEATURES,
                location.namespace
            )

            RecipeSerializer::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.RECIPE_SERIALIZERS,
                location.namespace
            )

            MenuType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.MENU_TYPES,
                location.namespace
            )

            Fluid::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.FLUIDS,
                location.namespace
            )

            //? if >=1.20.1 {
            CreativeModeTab::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                Registries.CREATIVE_MODE_TAB, location.namespace
            )
            //?}

            ParticleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.PARTICLE_TYPES,
                location.namespace
            )
            //? if >=1.21 {
            DataComponentType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.DATA_COMPONENT_TYPE.key(), location.namespace
            )
            //?}

            MobEffect::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.MOB_EFFECTS,
                location.namespace
            )

            Potion::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.POTIONS,
                location.namespace
            )

            PaintingVariant::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.PAINTING_VARIANTS,
                location.namespace
            )

            RecipeType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.RECIPE_TYPES,
                location.namespace
            )

            Attribute::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.ATTRIBUTES,
                location.namespace
            )

            StatType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.STAT_TYPES,
                location.namespace
            )

            ArgumentTypeInfo::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.COMMAND_ARGUMENT_TYPES,
                location.namespace
            )

            VillagerProfession::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.VILLAGER_PROFESSIONS,
                location.namespace
            )

            PoiType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.POI_TYPES,
                location.namespace
            )

            MemoryModuleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.MEMORY_MODULE_TYPES,
                location.namespace
            )

            SensorType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.SENSOR_TYPES,
                location.namespace
            )

            Schedule::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.SCHEDULES,
                location.namespace
            )

            Activity::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.ACTIVITIES,
                location.namespace
            )

            WorldCarver::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.WORLD_CARVERS,
                location.namespace
            )

            ChunkStatus::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.CHUNK_STATUS,
                location.namespace
            )

            BlockStateProviderType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES,
                location.namespace
            )

            FoliagePlacerType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.FOLIAGE_PLACER_TYPES,
                location.namespace
            )

            TreeDecoratorType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.TREE_DECORATOR_TYPES,
                location.namespace
            )

            registry != null -> DeferredRegister.create(registry.key(), location.namespace)

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as DeferredRegister<T>

    private val result: net.minecraftforge.registries.RegistryObject<T> = registryType.register(location.path, supplier).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addBlockModel(location, autoModel)

                if (IBlockItemProperties::class.java.isAssignableFrom(target)) {
                    val items: DeferredRegister<Item> =
                        DeferredRegister.create(ForgeRegistries.ITEMS, location.namespace)
                    items.register(
                        location.path
                    ) {
                        BlockItem(this.get() as Block, (this.get() as IBlockItemProperties).properties)
                    }
                    items.register(FMLJavaModLoadingContext.get().modEventBus)

                    if (autoModel != null)
                        HollowPack.addCustomItemModel(location, "{\"parent\":\"${ if (autoModel != AutoModelType.CUSTOM) "${location.namespace}:block/${location.path}" else autoModel.modelId}\"}")
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel != null) HollowPack.addItemModel(location, autoModel)
            }
        }
        registryType.register(FMLJavaModLoadingContext.get().modEventBus)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result.get() }
    }
}
*///?}
