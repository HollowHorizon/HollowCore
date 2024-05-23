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

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler
import ru.hollowhorizon.hc.client.utils.nbt.NBT_TAGS
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.network.register
import java.lang.annotation.ElementType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.jvm.optionals.getOrNull


object HollowModProcessor {
    private val ANNOTATIONS: HashMap<Type, (AnnotationContainer<*>) -> Unit> = hashMapOf()
    private var isInitialized = false

    @Suppress("UNCHECKED_CAST")
    private fun init() {
        if (isInitialized) return
        isInitialized = true
        registerHandler<HollowPacketV2> { cont ->
            cont.whenClassTask = { clazz ->
                if (HollowPacketV3::class.java in clazz.interfaces) clazz.register(cont.modId)
                else HollowCore.LOGGER.warn("Unsupported packet: ${clazz.simpleName}")
            }
        }

        registerHandler<Polymorphic> { content ->
            content.whenClassTask = {
                NBT_TAGS.computeIfAbsent(content.annotation.baseClass) { ArrayList() }.add(it.kotlin)
            }
        }
    }

    @JvmStatic
    fun initMod() {
        init()

        ModList.get().mods
            .filter { it.dependencies.any { dep -> dep.modId == HollowCore.MODID } || it.modId == HollowCore.MODID }
            .mapNotNull { ModList.get().getModContainerById(it.modId).getOrNull() }
            .forEach { run(it.modId, it.modInfo.owningFile.file.scanResult) }
    }

    @Synchronized
    private fun run(modId: String, scanResults: ModFileScanData) {
        HollowCore.LOGGER.info("Pre-loading: {}", modId)

        scanResults.classes.stream().filter { it.parent == Type.getType(HollowRegistry::class.java) }.forEach {
            HollowCore.LOGGER.info("Trying to load registerer class: {}", it.clazz)
            //Load kotlin class
            HollowRegistry.currentModId = modId
            Class.forName(it.clazz.className).getDeclaredField("INSTANCE").get(null) as HollowRegistry
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

            container.whenPropertyTask.invoke(field)
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

class AnnotationContainer<T : Any>(
    val modId: String,
    val annotation: T,
    val targetName: String,
) {
    var whenPropertyTask: (Field) -> Unit = {}
    var whenClassTask: (Class<*>) -> Unit = {}
}

