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
import net.minecraft.core.particles.ParticleType
import net.minecraft.resources.ResourceLocation
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
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.render.entity.RenderFactoryBuilder
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.isPhysicalClient
import ru.hollowhorizon.hc.common.objects.blocks.IBlockProperties
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class HollowRegistry {
    companion object {
        var currentModId = "hc"
    }

    //Avoid fake NotNulls parameters like BlockEntityType.Builder::build
    @Suppress("UNCHECKED_CAST")
    fun <T> promise(): T = null as T

    inline fun <reified T> register(
        config: ObjectConfig = ObjectConfig(),
        noinline registry: () -> T,
    ): RegistryHolder<T> {
        HollowCore.LOGGER.info("Registering: {}", config.name)
        return RegistryHolder(config, registry, T::class.java)
    }

    inline fun <reified T> register(objName: String, noinline registry: () -> T): RegistryHolder<T> {
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
class RegistryHolder<T>(private val config: ObjectConfig, supplier: () -> T, val target: Class<T>) {
    private val modId = HollowRegistry.currentModId

    val registry: DeferredRegister<T> = with(target) {
        when {
            Block::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(ForgeRegistries.BLOCKS, modId)
            Item::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(ForgeRegistries.ITEMS, modId)
            EntityType::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(
                ForgeRegistries.ENTITY_TYPES,
                modId
            )

            BlockEntityType::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(
                ForgeRegistries.BLOCK_ENTITY_TYPES,
                modId
            )

            SoundEvent::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(
                ForgeRegistries.SOUND_EVENTS,
                modId
            )

            Feature::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(ForgeRegistries.FEATURES, modId)
            RecipeSerializer::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(
                ForgeRegistries.RECIPE_SERIALIZERS,
                modId
            )

            MenuType::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(ForgeRegistries.MENU_TYPES, modId)
            ParticleType::class.java.isAssignableFrom(this) -> RegistryLoader.getRegistry(
                ForgeRegistries.PARTICLE_TYPES,
                modId
            )

            else -> throw UnsupportedOperationException("Unsupported registry object: ${target.simpleName}")
        }
    } as DeferredRegister<T>
    val result: RegistryObject<T> = registry.register(config.name, supplier).apply {
        when {
            Block::class.java.isAssignableFrom(target) -> {
                if (config.autoModel) HollowPack.genBlockData.add(ResourceLocation(modId, config.name))

                if (IBlockProperties::class.java.isAssignableFrom(target)) {
                    RegistryLoader.getRegistry(ForgeRegistries.ITEMS, modId).register(config.name) {
                        val data = this.get() as Block
                        BlockItem(data, (data as IBlockProperties).properties)
                    }
                }
            }

            Item::class.java.isAssignableFrom(target) -> {
                if (config.autoModel) HollowPack.genItemModels.add(ResourceLocation(modId, config.name))
            }

            EntityType::class.java.isAssignableFrom(target) -> {
                if (config.entityRenderer != null && isPhysicalClient) {
                    RenderFactoryBuilder.buildEntity(
                        { this.get() as EntityType<Entity> },
                        Class.forName(config.entityRenderer) as Class<EntityRenderer<Entity>>
                    )
                }
                val attributes = config.attributeSupplier
                if (attributes != null) {
                    MinecraftForge.EVENT_BUS.addListener<EntityAttributeCreationEvent> { event ->
                        event.put(this.get() as EntityType<LivingEntity>, attributes())
                    }
                }
            }

            BlockEntityType::class.java.isAssignableFrom(target) -> {
                if (config.blockEntityRenderer != null) {
                    RenderFactoryBuilder.buildTileEntity(
                        { this.get() as BlockEntityType<BlockEntity> },
                        config.blockEntityRenderer.java as Class<BlockEntityRenderer<BlockEntity>>
                    )
                }
            }
        }
    }


    operator fun getValue(thisRef: Any?, property: KProperty<*>): RegistryObject<T> {
        return result
    }
}

object RegistryLoader {
    private val REGISTRIES: MutableMap<IForgeRegistry<*>, MutableMap<String, DeferredRegister<*>>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <B> getRegistry(registryType: IForgeRegistry<B>, modId: String): DeferredRegister<B> {

        val registriesFor = REGISTRIES.computeIfAbsent(registryType) { ConcurrentHashMap() }

        val registry = registriesFor.computeIfAbsent(modId) {
            DeferredRegister.create(registryType, modId)
        }

        return registry as DeferredRegister<B>
    }

    @JvmStatic
    fun registerAll() {
        HollowCore.LOGGER.info("[RegistryLoader] Registering all registries.")
        REGISTRIES.values.forEach { registries ->
            registries.values.forEach { registry ->
                registry.register(MOD_CONTEXT.getKEventBus())
            }
        }

        REGISTRIES.clear()
    }
}
