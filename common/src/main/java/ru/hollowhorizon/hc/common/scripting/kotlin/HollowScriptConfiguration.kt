package ru.hollowhorizon.hc.common.scripting.kotlin

import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isProduction
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.classpathFromClassloader

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

@KotlinScript(
    "HollowScript", "ks.kts", compilationConfiguration = HollowScriptConfiguration::class
)
abstract class HollowScript

class AbstractHollowScriptHost : ScriptingHostConfiguration({
    getScriptingClass(JvmGetScriptingClass())
    classpathFromClassloader(Thread.currentThread().contextClassLoader)
})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target=17",
            "-Xadd-modules=ALL-MODULE-PATH" //Loading kotlin from shadowed jar
        )

        //Скорее всего в этом случае этот класс был загружен через IDE, поэтому получить моды и classpath автоматически нельзя
        if (true || isProduction) {
            dependenciesFromCurrentContext(wholeClasspath = true)
            return@jvm
        }

        //val stdLib = ModList.get().getModFileById(KotlinScriptForForge.MODID).file.filePath.toFile().absolutePath
        //System.setProperty("kotlin.java.stdlib.jar", stdLib)

        val files = HashSet<File>()

        //files.addAll(ModList.get().mods.map { File(it.owningFile.file.filePath.absolutePathString()) })
        //FMLLoader.getGamePath().resolve("mods").toFile().listFiles()?.forEach(files::add)
        //files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherModPaths.flatten().map { File(it.absolutePathString()) })
        //files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherArtifacts.map { File(it.absolutePathString()) })
        //files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.minecraftPaths.map { File(it.absolutePathString()) })

        dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)

        files.removeIf { it.isDirectory }
        updateClasspath(files.distinct().sortedBy { it.absolutePath }.onEach { HollowCore.LOGGER.info(it.absolutePath) })
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