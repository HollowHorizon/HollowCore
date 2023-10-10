package ru.hollowhorizon.hc.common.scripting.kotlin

import cpw.mods.modlauncher.TransformingClassLoader
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.client.utils.isProduction
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.JvmGetScriptingClass
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClassloader

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

@KotlinScript(
    "HollowScript", "hs.kts", compilationConfiguration = HollowScriptConfiguration::class
)
abstract class HollowScript

class AbstractHollowScriptHost : ScriptingHostConfiguration({
    getScriptingClass(JvmGetScriptingClass())
    classpathFromClassloader(TransformingClassLoader.getSystemClassLoader())
})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        val stdLib =
            if (isProduction) ModList.get().getModFileById("hc").file.filePath.toFile().absolutePath
            else File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\build\\libs\\hc-deps-kotlin-1.9.0.jar").absolutePath
        System.setProperty("kotlin.java.stdlib.jar", stdLib)

        val files = HashSet<File>()

        files.addAll(ModList.get().mods.map { File(it.owningFile.file.filePath.absolutePathString()) })
        FMLLoader.getGamePath().resolve("mods").toFile().listFiles()?.forEach(files::add)
        files.addAll(
            FMLLoader.getLaunchHandler().minecraftPaths.otherModPaths.flatten().map { File(it.absolutePathString()) })
        files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherArtifacts.map { File(it.absolutePathString()) })
        files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.minecraftPaths.map { File(it.absolutePathString()) })

        if (isProduction) {
            dependenciesFromClassContext(
                HollowScriptConfiguration::class,
                wholeClasspath = true
            )
        } else {
            dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)
        }

        files.removeIf { it.isDirectory }
        updateClasspath(files.distinct().sortedBy { it.absolutePath.also(::println) })

        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target=17",
            "-Xadd-modules=ALL-MODULE-PATH" //Loading kotlin from shadowed jar
        )

    }

    defaultImports(
        Import::class
    )

    refineConfiguration {
        onAnnotations(Import::class, handler = HollowScriptConfigurator())
    }

    ide { acceptedLocations(ScriptAcceptedLocation.Everywhere) }
})

class HollowScriptConfigurator : RefineScriptCompilationConfigurationHandler {
    override operator fun invoke(context: ScriptConfigurationRefinementContext) = processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile

        val importedSources = annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            } ?: emptyList()
        }

        return ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (importedSources.isNotEmpty()) importScripts.append(importedSources)
        }.asSuccess()
    }
}