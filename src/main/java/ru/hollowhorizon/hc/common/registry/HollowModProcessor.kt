package ru.hollowhorizon.hc.common.registry

import net.minecraft.block.Block
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.particles.ParticleType
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.world.gen.feature.structure.Structure
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.registy.HollowRegister
import ru.hollowhorizon.hc.api.utils.HollowCommand
import ru.hollowhorizon.hc.client.render.entity.RenderFactoryBuilder
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import ru.hollowhorizon.hc.common.commands.HollowCommands
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV2Loader
import ru.hollowhorizon.hc.common.network.Packet
import ru.hollowhorizon.hc.common.objects.blocks.IBlockProperties
import ru.hollowhorizon.hc.common.objects.items.HollowArmor
import ru.hollowhorizon.hc.core.AsmReflectionMethodGenerator
import ru.hollowhorizon.hc.core.ReflectionMethod
import java.lang.annotation.ElementType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


object HollowModProcessor {
    private val ANNOTATIONS: HashMap<Type, (AnnotationContainer<*>) -> Unit> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    fun init() {
        registerHandler<HollowRegister> { cont ->
            cont.whenObjectTask = whenObjectTask@{ field ->
                val name =
                    if (cont.annotation.value == "") cont.targetName.lowercase() else cont.annotation.value.lowercase()
                val data = field.get(null)
                val hasAutoModel = cont.annotation.auto_model
                val model = cont.annotation.renderer
                val hasModel = model != "" && FMLEnvironment.dist.isClient //всё что связано с рендером должно быть ТОЛЬКО на клиенте

                //По идеи регистрация дважды не должна происходить, но на всякий случай
                if (data is IForgeRegistryEntry<*> && data.registryName != null) {
                    return@whenObjectTask
                }

                when (data) {
                    is Block -> {
                        Registries.getRegistry(ForgeRegistries.BLOCKS, cont.modId).register(name) { data }

                        if (hasAutoModel) {
                            HollowPack.genBlockData.add(ResourceLocation(cont.modId, name))
                        }

                        if (data is IBlockProperties) {
                            Registries.getRegistry(ForgeRegistries.ITEMS, cont.modId)
                                .register(name) { BlockItem(data, data.properties) }
                        }
                    }

                    is Item -> {
                        Registries.getRegistry(ForgeRegistries.ITEMS, cont.modId).register(name) { data }
                        if (hasAutoModel) {
                            HollowPack.genItemModels.add(ResourceLocation(cont.modId, name))
                        }
                    }

                    is EntityType<*> -> {
                        Registries.getRegistry(ForgeRegistries.ENTITIES, cont.modId).register(name) { data }

                        if (hasModel) {
                            RenderFactoryBuilder.buildEntity(
                                data as EntityType<Entity>, Class.forName(model) as Class<EntityRenderer<Entity>>
                            )
                        }

                    }

                    is TileEntityType<*> -> {
                        Registries.getRegistry(ForgeRegistries.TILE_ENTITIES, cont.modId).register(name) { data }

                        if (hasModel) {
                            RenderFactoryBuilder.buildTileEntity(data as TileEntityType<TileEntity>, Class.forName(model) as Class<TileEntityRenderer<TileEntity>>)
                        }
                    }

                    is SoundEvent -> {
                        if (data.registryName != null) return@whenObjectTask
                        Registries.getRegistry(ForgeRegistries.SOUND_EVENTS, cont.modId).register(name) { data }
                    }

                    is Structure<*> -> {
                        Registries.getRegistry(ForgeRegistries.STRUCTURE_FEATURES, cont.modId).register(name) { data }
                    }

                    is HollowArmor<*> -> {
                        if (hasAutoModel) {
                            data.registerModels(cont.modId, name)
                        }
                        data.registerItems(Registries.getRegistry(ForgeRegistries.ITEMS, cont.modId), name)
                    }

                    is IRecipeSerializer<*> -> {
                        Registries.getRegistry(ForgeRegistries.RECIPE_SERIALIZERS, cont.modId).register(name) { data }
                    }

                    is ContainerType<*> -> {
                        Registries.getRegistry(ForgeRegistries.CONTAINERS, cont.modId).register(name) { data }

                        if (hasModel) {
                            RenderFactoryBuilder.buildContainerScreen(data as ContainerType<Container>, Class.forName(model) as Class<ContainerScreen<Container>>)
                        }
                    }

                    is ParticleType<*> -> {
                        Registries.getRegistry(ForgeRegistries.PARTICLE_TYPES, cont.modId).register(name) { data }
                    }

                }
            }
        }
        registerHandler<HollowPacketV2> { cont ->
            cont.whenClassTask = { clazz ->
                val packet = clazz.getConstructor().newInstance() as Packet<*>

                HollowPacketV2Loader.register(packet)
            }
        }
        registerHandler<HollowCapabilityV2> { cont ->
            cont.whenClassTask = { clazz ->
                HollowCapabilityStorageV2.capabilities.add(clazz)
            }
        }

        registerHandler<HollowCommand> { cont ->
            cont.whenMethodTask = { methodCaller ->
                val method = methodCaller()

                HollowCommands.addCommand(cont.annotation.value to Runnable {
                    method.invoke(null)
                })
            }
        }
    }

    @Synchronized
    fun run(modId: String, scanResults: ModFileScanData) {


        scanResults.annotations.stream().filter { it.annotationType in ANNOTATIONS.keys }.forEach { data ->
            val type = data.annotationType

            try {
                val containerClass = Class.forName(data.classType.className)

                when (data.targetType) {
                    ElementType.FIELD -> {
                        processField(containerClass, data.memberName, type, modId)
                    }

                    ElementType.METHOD -> {
                        processMethod(containerClass, data.memberName, type, modId)
                    }

                    ElementType.TYPE -> {
                        processClass(containerClass, type, modId)
                    }

                    else -> {
                        HollowCore.LOGGER.error("Annotation target type ${data.targetType} not supported! Only FIELD, METHOD and TYPE are supported!")
                    }
                }
            } catch (e: Exception) {
                HollowCore.LOGGER.info("Cant load class \"${data.classType.className}\". May be you forgot @OnlyIn(Dist.CLIENT) annotation?", e)
            }
        }

        if (FMLEnvironment.dist.isClient) processSounds(modId)

        Registries.registerAll()
    }

    @OnlyIn(Dist.CLIENT)
    private fun processSounds(modId: String) {
        HollowSoundHandler.MODS.add(modId)
    }

    @Suppress("UNCHECKED_CAST")
    private fun processField(containerClass: Class<*>, memberName: String, type: Type, modId: String) {
        val field = containerClass.getDeclaredField(memberName)

        field.isAccessible = true

        if (field.isStatic()) {
            val container = AnnotationContainer(
                modId, field.getAnnotation(Class.forName(type.className) as Class<Annotation>), memberName
            )

            ANNOTATIONS[type]?.invoke(container)

            container.whenObjectTask.invoke(field)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processMethod(containerClass: Class<*>, memberName: String, type: Type, modId: String) {
        val methods = containerClass.declaredMethods.filter { method ->
            method.annotations.map { Type.getType(it.javaClass) }.contains(type) && method.name == memberName
        }

        methods.forEach { method ->
            method.isAccessible = true

            if (method.isStatic()) {
                val container = AnnotationContainer(
                    modId, method.getAnnotation(Class.forName(type.className) as Class<Annotation>), method.name
                )

                ANNOTATIONS[type]?.invoke(container)

                container.whenMethodTask.invoke {
                    AsmReflectionMethodGenerator.generateMethod(method)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processClass(containerClass: Class<*>, type: Type, modId: String) {
        containerClass.getAnnotation(Class.forName(type.className) as Class<Annotation>)?.let { annotation ->
            val container = AnnotationContainer(modId, annotation, containerClass.name)

            ANNOTATIONS[type]?.invoke(container)

            container.whenClassTask.invoke(containerClass)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> registerHandler(noinline task: (AnnotationContainer<T>) -> Unit) {
        ANNOTATIONS[Type.getType(T::class.java)] = task as (AnnotationContainer<*>) -> Unit
    }

    fun postInit(modId: String) {
        if (modId != HollowCore.MODID) return

        //Обработчик NBT первый раз грузится долго, поэтому его лучше загрузить в отдельном потоке
        //Также важно, что он запускается ПОСЛЕ [HollowModProcessor], т.к. там добавляются данные для полиморфической сериализации!
        NBTFormat.init()
    }
}

private fun Field.isStatic(): Boolean {
    return Modifier.isStatic(this.modifiers)
}

private fun Method.isStatic(): Boolean {
    return Modifier.isStatic(this.modifiers)
}

object Registries {
    private val REGISTRIES: HashMap<IForgeRegistry<*>, HashMap<String, DeferredRegister<*>>> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <B : IForgeRegistryEntry<B>> getRegistry(registryType: IForgeRegistry<B>, modId: String): DeferredRegister<B> {
        val registriesFor = REGISTRIES.computeIfAbsent(registryType) { hashMapOf() }

        val registry = registriesFor.computeIfAbsent(modId) {
            DeferredRegister.create(registryType, modId)
        }

        return registry as DeferredRegister<B>
    }

    @JvmStatic
    fun registerAll() {
        REGISTRIES.values.forEach { registries ->
            registries.values.forEach { registry ->
                registry.register(FMLJavaModLoadingContext.get().modEventBus)
            }
        }

        REGISTRIES.clear()
    }
}

class AnnotationContainer<T : Any>(
    val modId: String,
    val annotation: T,
    val targetName: String,
) {
    var whenObjectTask: (Field) -> Unit = {}
    var whenClassTask: (Class<*>) -> Unit = {}
    var whenMethodTask: (() -> ReflectionMethod) -> Unit = {}
}

