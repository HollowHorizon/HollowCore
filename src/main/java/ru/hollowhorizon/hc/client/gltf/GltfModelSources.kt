package ru.hollowhorizon.hc.client.gltf

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.rl
import java.io.IOException
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
    override fun hasSource(data: String) = ResourceLocation.isValidResourceLocation(data)

    override fun getSource(data: String): InputStream {
        val rl = data.rl

        if(!HollowJavaUtils.hasResource(rl)) throw IOException("Model with path: $rl not found!")

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