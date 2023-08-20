package ru.hollowhorizon.hc.common.scripting.kotlin

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isIdeMode
import java.io.File
import java.net.URL
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        val stdLib =
            if (!isIdeMode && FMLLoader.isProduction()) ModList.get().getModFileById("hc").file.filePath.toFile().absolutePath
            else File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\build\\libs\\hc-1.1.0.jar").absolutePath
        System.setProperty(
            "kotlin.java.stdlib.jar",
            stdLib
        )

        val files = HashSet<File>()
        if (!isIdeMode && FMLLoader.isProduction()) {
            fun findClasspathEntry(cls: String): String {
                val classFilePath = "/${cls.replace('.', '/')}.class"
                val url = javaClass.getResource(classFilePath)
                    ?: throw RuntimeException("Failed to find $cls on classpath.")

                return when {
                    url.protocol == "jar" && url.file.endsWith("!$classFilePath") -> {
                        Paths.get(URL(url.file.removeSuffix("!$classFilePath")).toURI()).absolutePathString()
                    }

                    url.protocol == "file" && url.file.endsWith(classFilePath) -> {
                        var path = Paths.get(url.toURI())
                        repeat(cls.count { it == '.' } + 1) {
                            path = path.parent
                        }
                        path.absolutePathString()
                    }

                    else -> {
                        throw RuntimeException("Do not know how to turn $url into classpath entry.")
                    }
                }
            }

            files.addAll(ModList.get().modFiles.map { it.file.filePath.toFile() })

            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherModPaths.flatten().map { it.toFile() })
            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherArtifacts.map { it.toFile() })

            files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.minecraftPaths.map { it.toFile() })

            dependenciesFromClassContext(
                HollowScriptConfiguration::class,
                wholeClasspath = true
            )
        } else dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)

        updateClasspath(files.sortedBy { it.absolutePath }.apply { HollowCore.LOGGER.info("Updating classpath: {}", this) })

        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target=1.8",
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