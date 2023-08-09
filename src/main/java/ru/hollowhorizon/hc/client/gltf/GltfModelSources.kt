package ru.hollowhorizon.hc.client.gltf

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.rl
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

object GltfModelSources {
    private val SOURCES = hashSetOf<Source>(ResourceLocationSource())

    fun addSource(source: Source) = SOURCES.add(source)

    @JvmStatic
    fun getStream(string: String): InputStream {
        return SOURCES.first { it.hasSource(string) }.getSource(string)
    }
}

interface Source {
    fun hasSource(data: String): Boolean

    fun getSource(data: String): InputStream
}

class ResourceLocationSource : Source {
    override fun hasSource(data: String) =
        ResourceLocation.isValidResourceLocation(data) && HollowJavaUtils.hasResource(data.rl)

    override fun getSource(data: String): InputStream {
        return HollowJavaUtils.getResource(data.rl)
    }
}

class PathSource(val root: Path) : Source {
    override fun hasSource(data: String) = try {
        root.resolve(data).exists()
    } catch (e: Exception) {
        false
    }

    override fun getSource(data: String): InputStream = root.resolve(data).inputStream()

}