//? if fabric {
package ru.hollowhorizon.hc.fabric

import io.github.classgraph.ClassGraph
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider
import ru.hollowhorizon.hc.client.utils.isProduction
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes
import ru.hollowhorizon.hc.common.scripting.kotlin.deobfClassPath
import ru.hollowhorizon.hc.common.scripting.kotlin.scriptJars
import ru.hollowhorizon.hc.common.scripting.mappings.Remapper
import sun.misc.Unsafe
import java.nio.file.Path

private val unsafe by lazy {
    val theUnsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
    theUnsafe.isAccessible = true
    theUnsafe[null] as Unsafe
}

@Suppress("UNCHECKED_CAST")
fun <T> field(lookup: Any, name: String): T {
    val lookupClass = lookup::class.java
    val field = lookupClass.getDeclaredField(name) // Why did you have to make it private?
    val offset = unsafe.objectFieldOffset(field)
    return unsafe.getObject(lookup, offset) as T
}

object CoreInitializationFabric {
    init {
        val gameProvider =
            (FabricLoader.getInstance() as FabricLoaderImpl).gameProvider as MinecraftGameProvider
        val libs: List<Path> = field(gameProvider, "miscGameLibraries")
        val gameJars: List<Path> = field(gameProvider, "gameJars")
        val logJars: Set<Path> = field(gameProvider, "logJars")
        val parentClassPath: Collection<Path> = field(gameProvider, "validParentClassPath")

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
                graph.getSubclasses(it).filter { isClient || !it.name.contains("client") }
                    .map { Class.forName(it.name) }
                    .toSet()
            }
        getAnnotatedClasses =
            {
                graph.getClassesWithAnnotation(it as Class<out Annotation>)
                    .filter { isClient || !it.name.contains("client") }.map { Class.forName(it.name) }.toSet()
            }
        getAnnotatedMethods =
            { annotation ->
                val classes = graph.getClassesWithMethodAnnotation(annotation as Class<out Annotation>)

                classes.flatMap { it.methodInfo }
                    .filter { isClient || (!it.name.contains("client") && !it.className.contains("client")) }
                    .flatMap { type ->
                        Class.forName(type.className).declaredMethods.filter {
                            it.annotations.any {
                                annotation.isInstance(
                                    it
                                )
                            }
                        }
                    }.toSet()
            }

    }
}
//?}