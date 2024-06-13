package ru.hollowhorizon.hc

import io.github.classgraph.ClassGraph
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes

object CoreInitializationFabric {
    init {
        val graph = ClassGraph()
            .enableAllInfo()
            .scan()

        val isClient = FabricLoader.getInstance().environmentType == EnvType.CLIENT

        getSubTypes = {
            graph.getSubclasses(it).filter { isClient || !it.name.contains("client") }.map { Class.forName(it.name) }
                .toSet()
        }
        getAnnotatedClasses = {
            graph.getClassesWithAnnotation(it as Class<out Annotation>)
                .filter { isClient || !it.name.contains("client") }.map { Class.forName(it.name) }.toSet()
        }
        getAnnotatedMethods = { annotation ->
            val classes = graph.getClassesWithMethodAnnotation(annotation as Class<out Annotation>)

            classes.flatMap { it.methodInfo }
                .filter { isClient || (!it.name.contains("client") && !it.className.contains("client")) }
                .flatMap { type ->
                    Class.forName(type.className).declaredMethods.filter { it.annotations.any { annotation.isInstance(it) } }
                }.toSet()
        }

    }
}