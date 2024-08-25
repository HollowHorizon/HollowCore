package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.NativeImage
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toIS
import java.io.InputStream
import java.util.*

/**
 * A texture and its sampler.
 *
 * @param sampler The index of the sampler used by this texture. When undefined, a sampler with repeat wrapping and
 *                auto filtering should be used.
 * @param source  The index of the image used by this texture. When undefined, it is expected that an extension or
 *                other mechanism will supply an alternate texture source, otherwise behavior is undefined.
 * @param name    The user-defined name of this object.
 */
@Serializable
data class GltfTexture(
    val sampler: Int = -1,
    val source: Int = 0,
    val name: String? = null,
) {
    @Transient
    lateinit var imageRef: GltfImage

    @Transient
    var samplerRef: GltfSampler? = null

    @Transient
    private lateinit var createdTex: DynamicTexture

    fun makeTexture(location: ResourceLocation): ResourceLocation {
        val uri = imageRef.uri
        val name = if (uri != null && !uri.startsWith("data:", true)) {
            uri
        } else {
            val folderPath = location.path.substringBefore(".")
            "${location.namespace}:$folderPath/unnamed_texture_$source"
        }

        if (!this::createdTex.isInitialized) {
            if (uri != null && imageRef.bufferViewRef == null) {
                fun retrieveFile(path: String): InputStream {
                    if (path.startsWith("data:application/octet-stream;base64,")) {
                        return Base64.getDecoder().wrap(path.substring(37).byteInputStream())
                    }
                    if (path.startsWith("data:image/png;base64,")) {
                        return Base64.getDecoder().wrap(path.substring(22).byteInputStream())
                    }

                    return path.rl.toIS()
                }

                createdTex = DynamicTexture(NativeImage.read(retrieveFile(uri)))
            } else {
                createdTex = DynamicTexture(NativeImage.read(imageRef.bufferViewRef!!.getData().toArray()))
            }

            Minecraft.getInstance().textureManager.register(name.lowercase().rl, createdTex)
        }
        return name.lowercase().rl
    }
}