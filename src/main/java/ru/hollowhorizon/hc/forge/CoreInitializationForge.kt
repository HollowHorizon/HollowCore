//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.fml.ModList
import net.minecraftforge.forgespi.language.ModFileScanData
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes
import java.lang.annotation.ElementType
import java.lang.reflect.Method

object CoreInitializationForge {
    init {
        val scanInfo = ModList.get().mods
            .filter { mod -> mod.dependencies.any { it.modId == HollowCore.MODID } || mod.modId == HollowCore.MODID }
            .map { it.owningFile.file.scanResult }
        val classes = scanInfo.flatMap { it.classes }
        val annotations = scanInfo.flatMap { it.annotations }

        getSubTypes = { subType ->
            classes
                .filter { it.parent.className == subType.name }
                .safeClassesF().toSet()
        }
        getAnnotatedClasses = { annotation ->
            annotations
                .filter { it.annotationType.className == annotation.name }
                .filter { it.targetType == ElementType.TYPE }
                .safeClasses()
                .toSet()
        }
        getAnnotatedMethods = { annotation ->
            annotations
                .filter { it.annotationType.className == annotation.name }
                .filter { it.targetType == ElementType.METHOD }
                .safeMethods()
                .toSet()
        }
    }

    fun Collection<ModFileScanData.AnnotationData>.safeMethods(): List<Method> = mapNotNull {
        try {
            val name = it.memberName.substringBefore('(')
            Class.forName(it.clazz.className).declaredMethods
                .filter { m -> m.name == name }
        } catch (e: NoClassDefFoundError) {
            HollowCore.LOGGER.warn("Class ${it.clazz.className} cannot be loaded! ${e.message}")
            null
        } catch (e: ClassNotFoundException) {
            HollowCore.LOGGER.warn("Class ${it.clazz.className} cannot be loaded! ${e.message}")
            null
        }
    }.flatten()

    private fun Collection<ModFileScanData.AnnotationData>.safeClasses(): List<Class<*>> = mapNotNull {
        try {
            Class.forName(it.clazz.className)
        } catch (e: NoClassDefFoundError) {
            HollowCore.LOGGER.warn("Class ${it.clazz.className} cannot be loaded! ${e.message}")
            null
        }
    }
    private fun Collection<ModFileScanData.ClassData>.safeClassesF(): List<Class<*>> = mapNotNull {
        try {
            Class.forName(it.clazz.className)
        } catch (e: NoClassDefFoundError) {
            HollowCore.LOGGER.warn("Class ${it.clazz.className} cannot be loaded! ${e.message}")
            null
        }
    }
}
*///?}