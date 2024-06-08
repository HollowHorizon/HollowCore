package ru.hollowhorizon.hc

import io.github.classgraph.ClassGraph
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes

object CoreInitialization {
    init {
        val graph = ClassGraph()
            .enableAllInfo()
            .scan()

        getSubTypes = {
            graph.getSubclasses(it).map { Class.forName(it.name) }.toSet()
        }
        getAnnotatedClasses = {
            graph.getClassesWithAnnotation(it as Class<out Annotation>).map { Class.forName(it.name) }.toSet()
        }
        getAnnotatedMethods = { annotation ->
            val classes = graph.getClassesWithMethodAnnotation(annotation as Class<out Annotation>)

            classes.flatMap { it.methodInfo }.flatMap { type ->
                Class.forName(type.className).declaredMethods.filter { it.annotations.any { annotation.isInstance(it) } }
            }.toSet()
        }

    }
}