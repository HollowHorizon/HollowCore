//? if fabric {
package ru.hollowhorizon.hc.fabric

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isProduction
import ru.hollowhorizon.hc.common.events.ClientOnly
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes
import ru.hollowhorizon.hc.common.scripting.kotlin.deobfClassPath
import ru.hollowhorizon.hc.common.scripting.kotlin.scriptJars
import ru.hollowhorizon.hc.common.scripting.mappings.Remapper
import sun.misc.Unsafe
import java.lang.reflect.Method
import java.nio.file.Path

private val unsafe by lazy {
    val theUnsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
    theUnsafe.isAccessible = true
    theUnsafe[null] as Unsafe
}

@Suppress("UNCHECKED_CAST")
fun <T> findField(lookup: Any, name: String): T {
    val lookupClass = lookup::class.java
    val field = lookupClass.getDeclaredField(name) // Why did you have to make it private?
    val offset = unsafe.objectFieldOffset(field)
    return unsafe.getObject(lookup, offset) as T
}

object CoreInitializationFabric {
    init {
        val gameProvider =
            (FabricLoader.getInstance() as FabricLoaderImpl).gameProvider as MinecraftGameProvider
        val libs: List<Path> = findField(gameProvider, "miscGameLibraries")
        val gameJars: List<Path> = findField(gameProvider, "gameJars")
        val logJars: Set<Path> = findField(gameProvider, "logJars")
        val parentClassPath: Collection<Path> = findField(gameProvider, "validParentClassPath")

        if (isProduction) {

            Remapper.remap(
                Remapper.DEOBFUSCATE_REMAPPER,
                gameJars.map { it.toFile() }.toTypedArray(),
                deobfClassPath.toPath()
            )

            scriptJars.addAll((libs + logJars + parentClassPath).map { it.toFile() })
        } else {
            scriptJars.addAll((libs + gameJars + logJars + parentClassPath).map { it.toFile() })
        }

        val graph = ClassGraph()
            .enableAllInfo()
            .scan()

        val isClient = FabricLoader.getInstance().environmentType == EnvType.CLIENT

        getSubTypes =
            {
                graph.getSubclasses(it)
                    .filter { isClient || !it.annotationInfo.all { it.name != ClientOnly::class.java.name } }
                    .safeClasses().toSet()
            }
        getAnnotatedClasses =
            {
                graph.getClassesWithAnnotation(it as Class<out Annotation>)
                    .filter { isClient || it.annotationInfo.all { it.name != ClientOnly::class.java.name } }
                    .safeClasses().toSet()
            }
        getAnnotatedMethods =
            { annotation ->
                val classes = graph.getClassesWithMethodAnnotation(annotation as Class<out Annotation>)

                classes.flatMap { it.methodInfo }
                    .filter { it.annotationInfo.any { it.name == annotation.name } }
                    .filter { isClient || it.annotationInfo.all { it.name != ClientOnly::class.java.name } }
                    .filter { isClient || it.classInfo.annotationInfo.all { it.name != ClientOnly::class.java.name } }
                    .safeMethods(annotation)
                    .toSet()
            }

    }

    fun Collection<MethodInfo>.safeMethods(annotation: Class<*>): List<Method> = mapNotNull {
        try {
            Class.forName(it.className).declaredMethods.filter {
                it.annotations.any { annotation.isInstance(it) }
            }
        } catch (e: NoClassDefFoundError) {
            HollowCore.LOGGER.warn("Class ${it.className} cannot be loaded! ${e.message}")
            null
        } catch (e: ClassNotFoundException) {
            HollowCore.LOGGER.warn("Class ${it.className} cannot be loaded! ${e.message}")
            null
        }
    }.flatten()

    private fun Collection<ClassInfo>.safeClasses(): List<Class<*>> = mapNotNull {
        try {
            Class.forName(it.name)
        } catch (e: NoClassDefFoundError) {
            HollowCore.LOGGER.warn("Class ${it.name} cannot be loaded! ${e.message}")
            null
        }
    }
}
//?}