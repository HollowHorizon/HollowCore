//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isProduction
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes
import ru.hollowhorizon.hc.common.scripting.kotlin.deobfClassPath
import ru.hollowhorizon.hc.common.scripting.kotlin.scriptJars
import ru.hollowhorizon.hc.common.scripting.mappings.Remapper
import java.lang.annotation.ElementType
import kotlin.script.experimental.jvm.util.classPathFromTypicalResourceUrls

object CoreInitializationForge {
    init {
        val scanInfo = ModList.get().mods
            .filter { mod -> mod.dependencies.any { it.modId == HollowCore.MODID } || mod.modId == HollowCore.MODID }
            .map { it.owningFile.file.scanResult }
        val classes = scanInfo.flatMap { it.classes }
        val annotations = scanInfo.flatMap { it.annotations }

        getSubTypes = { subType ->
            classes.filter { it.parent.className == subType.name }.map { Class.forName(it.clazz.className) }.toSet()
        }
        getAnnotatedClasses = { annotation ->
            annotations
                .filter { it.annotationType.className == annotation.name }
                .filter { it.targetType == ElementType.TYPE }
                .map { Class.forName(it.clazz.className) }
                .toSet()
        }
        getAnnotatedMethods = { annotation ->
            annotations
                .filter { it.annotationType.className == annotation.name }
                .filter { it.targetType == ElementType.METHOD }
                .flatMap {
                    val name = it.memberName.substringBefore('(')
                    Class.forName(it.clazz.className).declaredMethods
                        .filter { m -> m.name == name }
                }
                .toSet()
        }
    }
}
*///?}