//? if fabric {
/*package ru.hollowhorizon.hc.fabric.internal

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import ru.hollowhorizon.hc.client.utils.ModList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.jar.JarFile
import kotlin.jvm.optionals.getOrNull

object FabricModList : ModList {
    override fun isLoaded(modId: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    override fun getFile(modId: String): File {
        return FabricLoader.getInstance().getModFile(modId)
    }

    override val mods: List<String>
        get() = FabricLoader.getInstance().allMods.map { it.metadata.id }
}

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
*///?}