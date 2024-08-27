package ru.hollowhorizon.hc.forge.internal

//? if forge && >=1.21 {
import net.minecraft.core.component.DataComponentType
//?}

//? if forge {

import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderForge<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: Boolean,
    supplier: () -> T,
    val target: Class<T>,
) :
    IRegistryHolder<T> {
    val registryType: DeferredRegister<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.ITEMS,
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

            CreativeModeTab::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                Registries.CREATIVE_MODE_TAB, location.namespace
            )

            ParticleType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                ForgeRegistries.PARTICLE_TYPES,
                location.namespace
            )
            //? if >=1.21 {
            DataComponentType::class.java.isAssignableFrom(this) -> DeferredRegister.create(
                BuiltInRegistries.DATA_COMPONENT_TYPE.key(), location.namespace
            )
            //?}

            registry != null -> DeferredRegister.create(registry.key(), location.namespace)

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as DeferredRegister<T>

    private val result: net.minecraftforge.registries.RegistryObject<T> = registryType.register(location.path, supplier).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.genBlockData.add(location)

                if (IBlockItemProperties::class.java.isAssignableFrom(target)) {
                    val items: DeferredRegister<Item> =
                        DeferredRegister.create(ForgeRegistries.ITEMS, location.namespace)
                    items.register(
                        location.path
                    ) {
                        BlockItem(this as Block, (this as IBlockItemProperties).properties)
                    }
                    items.register(FMLJavaModLoadingContext.get().modEventBus)
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.genItemModels.add(location)
            }
        }
        registryType.register(FMLJavaModLoadingContext.get().modEventBus)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result.get() }
    }
}
//?}