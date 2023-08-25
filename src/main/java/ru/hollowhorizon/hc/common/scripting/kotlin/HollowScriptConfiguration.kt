package ru.hollowhorizon.hc.common.scripting.kotlin

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isIdeMode
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        val stdLib =
            if (!isIdeMode && FMLLoader.isProduction()) ModList.get()
                .getModFileById("hc").file.filePath.toFile().absolutePath
            else File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\build\\libs\\hc-1.1.0.jar").absolutePath
        System.setProperty(
            "kotlin.java.stdlib.jar",
            stdLib
        )

        val files = HashSet<File>()
        if (!isIdeMode && FMLLoader.isProduction()) {
            files.addAll(ModList.get().modFiles.map { it.file.filePath.toRealPath().toFile() })

            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherModPaths.flatten().map { it.toRealPath().toFile() })
            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherArtifacts.map { it.toRealPath().toFile() })

            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.minecraftPaths.map {
                HollowCore.LOGGER.info("Trying to add dependency from ${it.absolutePathString()}, will success: ${it.fileSystem === FileSystems.getDefault()}")
                it.toRealPath().toFile()
            })

            dependenciesFromClassContext(
                HollowScriptConfiguration::class,
                wholeClasspath = true
            )
        } else {
            if(isIdeMode) {
                files.addAll(File("C:\\Users\\user\\Twitch\\Minecraft\\Instances\\Instances\\test1\\intellij_idea.classpath").readLines().map { File(it) })
            }
            dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)
        }

        updateClasspath(files.sortedBy { it.absolutePath })

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

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
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