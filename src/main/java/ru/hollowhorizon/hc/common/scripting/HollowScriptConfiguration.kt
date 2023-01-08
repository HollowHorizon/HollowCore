package ru.hollowhorizon.hc.common.scripting

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

class HollowScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)

        val files = ArrayList<File>()
        if (FMLLoader.isProduction()) {
            files.addAll(ModList.get().modFiles.map { it.file.filePath.toFile() })
            files.add(FMLLoader.getForgePath().toFile())
            files.addAll(FMLLoader.getMCPaths().map { it.toFile() })
        }

        updateClasspath(files)


        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target", "1.8",
        )


    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
})