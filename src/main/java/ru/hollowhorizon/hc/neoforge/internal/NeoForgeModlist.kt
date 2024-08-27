//? if neoforge {
/*package ru.hollowhorizon.hc.neoforge.internal

import ru.hollowhorizon.hc.client.utils.ModList
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object NeoForgeModList : ModList {
    val forgeList = net.neoforged.fml.ModList.get()
    override fun isLoaded(modId: String): Boolean {
        return forgeList.isLoaded(modId)
    }

    override fun getFile(modId: String): File {
        val path = forgeList.getModFileById(modId).file.filePath
        return getAsFile(path, modId).first()
    }

    fun getFiles(modId: String): List<File> {
        val path = forgeList.getModFileById(modId).file.filePath
        return getAsFile(path, modId)
    }

    override val mods: List<String>
        get() = forgeList.mods.map { it.modId }
}

fun streamUnionPath(path: Path): InputStream {
    val stream = path::class.java.getDeclaredMethod("buildInputStream")

    return stream.invoke(path) as InputStream
}

fun getAsFile(path: Path, modId: String): List<File> {
    try {
        var fileName = path.fileName.toString()
        if (!fileName.endsWith(".jar")) fileName = "$modId.jar"

        val copy = Files.newInputStream(path)

        val newFile = File("hollowcore/embed_mods/$fileName").apply {
            if (!this.parentFile.exists()) this.parentFile.mkdirs()
        }

        if (newFile.exists()) return listOf(newFile)
        else newFile.createNewFile()

        FileOutputStream(newFile).use { jarOutput ->
            copy.use { reader ->
                reader.copyTo(jarOutput)
            }
        }

        return listOf(newFile)
    } catch (e: AccessDeniedException) {
        return listOf(File(e.file))
    } catch (e: Exception) {
        if(path.fileSystem::class.java.name == "cpw.mods.niofs.union.UnionFileSystem") {
            val system = path.fileSystem::class.java.getDeclaredField("basepaths")
            system.isAccessible = true
            return (system.get(path.fileSystem) as List<Path>).map { it.toFile() }
        }

        return listOf(File(path.absolutePathString()))
    }
}
*///?}