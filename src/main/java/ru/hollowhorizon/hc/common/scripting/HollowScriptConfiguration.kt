package ru.hollowhorizon.hc.common.scripting

import cpw.mods.modlauncher.TransformingClassLoader
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.scripting.annotations.HollowScriptConfigurator
import ru.hollowhorizon.hc.common.scripting.annotations.Import
import java.io.File
import java.net.URL
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        if (FMLLoader.isProduction()) System.setProperty(
            "kotlin.java.stdlib.jar",
            ModList.get().getModFileById("hc").file.filePath.toFile().absolutePath
        )

        val files = ArrayList<File>()
        if (FMLLoader.isProduction()) {
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
            files.add(FMLLoader.getForgePath().toFile())
            files.addAll(FMLLoader.getMCPaths().map { it.toFile() })

            var libraries = FMLLoader.getGamePath().resolve("libraries").toFile().walk().filter { it.name.endsWith(".jar") }.toList()

            if(libraries.isEmpty()) {
                //Такое может произойти, если какой-то умник засунул папку с библиотеками не пойми куда, например как это сделали в CurseForge App

                var exampleLibrary = File(findClasspathEntry("org.apache.logging.log4j.Logger")) //Попробуем найти библиотеку, которая точно существует
                while (exampleLibrary.name != "libraries") {
                    if(exampleLibrary.parentFile == null) break
                    exampleLibrary = exampleLibrary.parentFile
                }

                if(exampleLibrary.name == "libraries") {
                    libraries = exampleLibrary.walk().filter { it.name.endsWith(".jar") }.toList()
                } else {
                    HollowCore.LOGGER.error("Failed to find libraries folder!")
                }
            }
            files.addAll(libraries)

            dependenciesFromClassloader(classLoader = TransformingClassLoader.getSystemClassLoader())
        } else dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)

        updateClasspath(files)


        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target", "1.8",
        )

    }

    defaultImports(
        "ru.hollowhorizon.hc.common.scripting.annotations.Import",
        "kotlin.script.experimental.dependencies.DependsOn",
        "kotlin.script.experimental.dependencies.Repository"
    )

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, Import::class, handler = HollowScriptConfigurator())
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
})