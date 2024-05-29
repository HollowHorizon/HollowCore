/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.registry

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.feature.Feature
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.HollowCoreEvents
import ru.hollowhorizon.hc.client.render.entity.RenderFactoryBuilder
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.isPhysicalClient
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class HollowRegistry {
    companion object {
        var currentModId = "hc"
    }

    //Avoid fake NotNulls parameters like BlockEntityType.Builder::build
    @Suppress("UNCHECKED_CAST")
    fun <T> promise(): T = null as T

    inline fun <reified T : Any> register(
        config: ObjectConfig = ObjectConfig(),
        noinline registry: () -> T,
    ): RegistryHolder<T> {
        HollowCore.LOGGER.info("Registering: {}", config.name)
        return RegistryHolder(config, registry, T::class.java)
    }

    inline fun <reified T : Any> register(objName: String, noinline registry: () -> T): RegistryHolder<T> {
        return register(ObjectConfig(name = objName), registry)
    }
}

data class ObjectConfig(
    var name: String = "",
    val autoModel: Boolean = true,
    val entityRenderer: String? = null,
    val blockEntityRenderer: KClass<*>? = null,
    val attributeSupplier: (() -> AttributeSupplier)? = null,
)

@Suppress("UNCHECKED_CAST")
class RegistryHolder<T : Any>(config: ObjectConfig, supplier: () -> T, val target: Class<T>) {
    val registryType: Registry<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK
            Item::class.java.isAssignableFrom(this) -> BuiltInRegistries.ITEM
            EntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.ENTITY_TYPE
            BlockEntityType::class.java.isAssignableFrom(this) -> BuiltInRegistries.BLOCK_ENTITY_TYPE
            SoundEvent::class.java.isAssignableFrom(this) -> BuiltInRegistries.SOUND_EVENT
            Feature::class.java.isAssignableFrom(this) -> BuiltInRegistries.FEATURE
            RecipeSerializer::class.java.isAssignableFrom(this) -> BuiltInRegistries.RECIPE_SERIALIZER

            MenuType::class.java.isAssignableFrom(this) -> BuiltInRegistries.MENU
            ParticleType::class.java.isAssignableFrom(this) -> BuiltInRegistries.PARTICLE_TYPE

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as Registry<T>

    private val result: T = Registry.register(registryType, config.name.rl, supplier()).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (config.autoModel) HollowPack.genBlockData.add(config.name.rl)

                if (IBlockItemProperties::class.java.isAssignableFrom(target)) {
                    Registry.register(
                        BuiltInRegistries.ITEM,
                        config.name.rl,
                        BlockItem(this as Block, (this as IBlockItemProperties).properties)
                    )
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (config.autoModel) HollowPack.genItemModels.add(config.name.rl)
            }

            EntityType::class.java.isAssignableFrom(target) -> {
                if (config.entityRenderer != null && isPhysicalClient) {
                    RenderFactoryBuilder.buildEntity(
                        { this as EntityType<Entity> },
                        Class.forName(config.entityRenderer) as Class<EntityRenderer<Entity>>
                    )
                }
                val attributes = config.attributeSupplier
                if (attributes != null) {
                    HollowCoreEvents.registerAttributesEvent(this as EntityType<LivingEntity>, attributes())
                }
            }

            BlockEntityType::class.java.isAssignableFrom(target) -> {
                if (config.blockEntityRenderer != null) {
                    RenderFactoryBuilder.buildTileEntity(
                        { this as BlockEntityType<BlockEntity> },
                        config.blockEntityRenderer.java as Class<BlockEntityRenderer<BlockEntity>>
                    )
                }
            }
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return RegistryObject { result }
    }
}

fun interface RegistryObject<T> {
    fun get(): T
}