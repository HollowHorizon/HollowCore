package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation

/**
 * Reference to a texture (color, emissive, normal, occlusion).
 *
 * @param index    The index of the texture.
 * @param strength A scalar multiplier controlling the amount of occlusion applied (for occlusion textures only).
 * @param texCoord The set index of texture's TEXCOORD attribute used for texture coordinate mapping.
 * @param scale    The scalar multiplier applied to each normal vector of the normal texture.
 */
@Serializable
data class GltfTextureInfo(
    val index: Int,
    val strength: Float = 1f,
    val texCoord: Int = 0,
    val scale: Float = 1f,
) {
    fun getTexture(gltfFile: GltfFile, location: ResourceLocation): ResourceLocation {
        return gltfFile.textures[index].makeTexture(location)
    }
}