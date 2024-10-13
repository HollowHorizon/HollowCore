package ru.hollowhorizon.hc.client.utils

//? if fabric {
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.FileNotFoundException
import java.util.jar.JarFile
import kotlin.jvm.optionals.getOrNull
//?} elif forge {
/*import net.minecraftforge.fml.ModList
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
*///?}
import java.io.File
import java.io.FileOutputStream

object ModList {
    fun isLoaded(modId: String): Boolean {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(modId)
        //?} elif forge || neoforge {
        /*return ModList.get().isLoaded(modId)
        *///?}
    }

    fun getFile(modId: String): File {
        //? if fabric {
        return FabricLoader.getInstance().getModFile(modId)
        //?} elif forge || neoforge {
        /*// Не уже ли так сложно просто дать нормальный путь к файлу...
        val path = ModList.get().getModFileById(modId).file.filePath
        return getAsFile(path, modId).first()
        *///?}
    }

    val mods: List<String>
        get() {
            //? if fabric {
            return FabricLoader.getInstance().allMods.map { it.metadata.id }
            //?} elif forge || neoforge {
            /*return ModList.get().mods.map { it.modId }
            *///?}
        }

}

//? if fabric {
fun FabricLoader.getModFile(modId: String): File {
    val modContainer = getModContainer(modId).getOrNull()
        ?: throw FileNotFoundException("Mod Not Found: $modId")
    val origin = modContainer.origin

    return when (val kind = origin.kind) {
        ModOrigin.Kind.PATH -> origin.paths[0].toFile()
        ModOrigin.Kind.NESTED -> getNestedModFile(origin)
        else -> throw IllegalStateException("Unsupported kind: $kind")
    }
}

fun FabricLoader.getNestedModFile(origin: ModOrigin): File {

    val parentId = origin.parentModId
    val parentFile = getModFile(parentId)
    val subLocation = origin.parentSubLocation

    val parentJar = JarFile(parentFile)
    val nestedJarEntry = parentJar.getJarEntry(subLocation)

    val fileName = subLocation.split('/').last()
    val newFile = File("hollowcore/embed_mods/$fileName").apply {
        if (!this.parentFile.exists()) this.parentFile.mkdirs()
    }

    if (newFile.exists()) return newFile
    else newFile.createNewFile()

    FileOutputStream(newFile).use { jarOutput ->
        parentJar.getInputStream(nestedJarEntry).use { reader ->
            reader.copyTo(jarOutput)
        }
    }

    return newFile
}
//?} elif forge || neoforge {
/*fun getAsFile(path: Path, modId: String): List<File> {
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
        return listOf(e.file)
    } catch (e: Exception) {
        if (path.fileSystem::class.java.name == "cpw.mods.niofs.union.UnionFileSystem") {
            val system = path.fileSystem::class.java.getDeclaredField("basepaths")
            system.isAccessible = true
            return (system.get(path.fileSystem) as List<Path>).map { it.toFile() }
        }

        return listOf(File(path.absolutePathString()))
    }
}
*///?}