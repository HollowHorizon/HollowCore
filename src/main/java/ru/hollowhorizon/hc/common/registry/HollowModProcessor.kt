package ru.hollowhorizon.hc.common.registry

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.IForgeRegistry
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.utils.HollowCommand
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler
import ru.hollowhorizon.hc.common.commands.HollowCommands
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV2Loader
import ru.hollowhorizon.hc.common.network.Packet
import ru.hollowhorizon.hc.core.AsmReflectionMethodGenerator
import ru.hollowhorizon.hc.core.ReflectionMethod
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT
import java.lang.annotation.ElementType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


object HollowModProcessor {
    private val ANNOTATIONS: HashMap<Type, (AnnotationContainer<*>) -> Unit> = hashMapOf()
    private var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    private fun init() {
        if (isInitialized) return
        isInitialized = true
        registerHandler<HollowPacketV2> { cont ->
            cont.whenClassTask = { clazz ->
                HollowCore.LOGGER.info("Loading packet: ${clazz.simpleName}")

                val packet = clazz.getConstructor().newInstance() as Packet<*>

                HollowPacketV2Loader.register(packet, cont.annotation.toTarget)
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

    @JvmStatic
    fun initMod() {
        init()

        val container = ModLoadingContext.get().activeContainer

        run(container.modId, container.modInfo.owningFile.file.scanResult)
    }

    @Synchronized
    private fun run(modId: String, scanResults: ModFileScanData) {
        HollowCore.LOGGER.info("Pre-loading: {}", modId)

        scanResults.classes.stream().filter { it.parent == Type.getType(HollowRegistry::class.java) }.forEach {
            HollowCore.LOGGER.info("Trying to load registerer class: {}", it.clazz)
            //Load kotlin class
            Class.forName(it.clazz.className).getDeclaredField("INSTANCE").get(null)
        }
        scanResults.annotations.stream().filter { it.annotationType in ANNOTATIONS.keys }.forEach { data ->
            val type = data.annotationType


            val containerClass = Class.forName(data.clazz.className)

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
    fun <B> getRegistry(registryType: IForgeRegistry<B>, modId: String): DeferredRegister<B> {
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
                registry.register(MOD_CONTEXT.getKEventBus())
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

