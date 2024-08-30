//? if fabric {
/*package ru.hollowhorizon.hc.fabric.internal

import net.minecraft.core.Registry
//? if >1.21 {
/^import net.minecraft.core.component.DataComponentType
^///?}

//? if >=1.20.1
/^import net.minecraft.core.registries.BuiltInRegistries^/
import net.minecraft.core.particles.ParticleType
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
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import ru.hollowhorizon.hc.common.registry.IRegistryHolder
import ru.hollowhorizon.hc.common.registry.RegistryObject
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class RegistryHolderFabric<T : Any>(
    val location: ResourceLocation,
    val registry: Registry<T>? = null,
    val autoModel: Boolean,
    supplier: () -> T,
    val target: Class<T>,
) :
    IRegistryHolder<T> {
    val registryType: Registry<T> = with(target) {
        when {
            //? if >=1.20.1 {
            /^Block::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK
            Item::class.java.isAssignableFrom(this) -> BuiltInRegistries.ITEM
            EntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.ENTITY_TYPE
            BlockEntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK_ENTITY_TYPE
            SoundEvent::class.java.isAssignableFrom(this) -> BuiltInRegistries.SOUND_EVENT
            Feature::class.java.isAssignableFrom(this) -> BuiltInRegistries.FEATURE
            RecipeSerializer::class.java.isAssignableFrom(this) -> BuiltInRegistries.RECIPE_SERIALIZER
            CreativeModeTab::class.java.isAssignableFrom(this) -> BuiltInRegistries.CREATIVE_MODE_TAB
            MenuType::class.java.isAssignableFrom(this) -> BuiltInRegistries.MENU
            ParticleType::class.java.isAssignableFrom(this) -> BuiltInRegistries.PARTICLE_TYPE
            ^///?} else {
            Block::class.java.isAssignableFrom(this) -> Registry.BLOCK
            Item::class.java.isAssignableFrom(this) -> Registry.ITEM
            EntityType::class.java.isAssignableFrom(this) -> Registry.ENTITY_TYPE
            BlockEntityType::class.java.isAssignableFrom(this) -> Registry.BLOCK_ENTITY_TYPE
            SoundEvent::class.java.isAssignableFrom(this) -> Registry.SOUND_EVENT
            Feature::class.java.isAssignableFrom(this) -> Registry.FEATURE
            RecipeSerializer::class.java.isAssignableFrom(this) -> Registry.RECIPE_SERIALIZER
            MenuType::class.java.isAssignableFrom(this) -> Registry.MENU
            ParticleType::class.java.isAssignableFrom(this) -> Registry.PARTICLE_TYPE
            //?}

            //? if >1.21 {
            /^DataComponentType::class.java.isAssignableFrom(this) -> BuiltInRegistries.DATA_COMPONENT_TYPE
            ^///?}

            registry != null -> registry

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as Registry<T>

    private val result: T = Registry.register(registryType, location, supplier()).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.addBlockModel(location)

                if (IBlockItemProperties::class.java.isAssignableFrom(target)) {
                    Registry.register(
                        /^? if >=1.20.1 {^//^BuiltInRegistries.ITEM^//^?} else {^/Registry.ITEM/^?}^/,
                        location,
                        BlockItem(this as Block, (this as IBlockItemProperties).properties)
                    )
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (autoModel) HollowPack.addItemModel(location)
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result }
    }
}
*///?}