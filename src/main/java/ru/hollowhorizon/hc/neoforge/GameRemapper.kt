package ru.hollowhorizon.hc.neoforge

//? if neoforge {

/*import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.neoforged.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.isProduction
import ru.hollowhorizon.hc.common.scripting.kotlin.deobfClassPath
import ru.hollowhorizon.hc.common.scripting.kotlin.scriptJars
import ru.hollowhorizon.hc.common.scripting.mappings.Remapper
import java.io.File
import java.nio.file.Files
import java.util.jar.JarFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object GameRemapper {
    fun remap() {
        val classPath = System.getProperty("java.class.path")
            .split(";")
            .map { File(it) }
            .toMutableSet()


        val jarsToRemap =
            FMLLoader.getLoadingModList().modFiles
                .filter { it.mods.any { it.modId in HollowCore.config.scripting.includeMods } }
                .map { it.file.secureJar.primaryPath }.map { it.toFile() }
                .toMutableSet()

        jarsToRemap
            .filter { it.isFile && it.name.endsWith(".jar") }
            .forEach { collectJars(it) }

        Remapper.remap(
            Remapper.DEOBFUSCATE_REMAPPER,
            File("hollowcore/embed_mods/").walk().filter { it.isFile && it.name.endsWith(".jar") }.toList()
                .toTypedArray(),
            deobfClassPath.toPath()
        )

        val toRemove = jarsToRemap.map { it.absolutePath }.toSet()
        classPath.removeIf { it.absolutePath in toRemove }

        scriptJars.addAll(classPath)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun collectJars(file: File) {
        val mod = embedMods.resolve(file.name)
        if (!mod.exists()) Files.copy(file.toPath(), mod)

        val jar = JarFile(file)
        val stream = Json.decodeFromStream<JarInJars>(
            jar.getInputStream(
                jar.getEntry("META-INF/jarjar/metadata.json") ?: return
            )
        )

        stream.jars.forEach {
            val embedMod = embedMods.resolve(it.path.substringAfterLast("/"))
            val entry = jar.getInputStream(jar.getEntry(it.path))
            if (!embedMod.exists()) Files.copy(entry, embedMod)
            collectJars(embedMod.toFile())
        }
    }

    private val embedMods = File("hollowcore/embed_mods/").apply {
        if (!exists()) this.mkdirs()
    }.toPath()

    @Serializable
    class JarInJars(
        val jars: List<Jar>,
    ) {
        @Serializable
        class Jar(
            val identifier: Identifier,
            val version: Version,
            val path: String,
        ) {
            @Serializable
            data class Identifier(val group: String, val artifact: String)

            @Serializable
            data class Version(val range: String, val artifactVersion: String)
        }
    }
}
*///?}