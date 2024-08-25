package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.models.internal.Material

/**
 * The material appearance of a primitive.
 *
 * @param name                 The user-defined name of this object.
 * @param pbrMetallicRoughness A set of parameter values that are used to define the metallic-roughness material model
 *                             from Physically-Based Rendering (PBR) methodology. When not specified, all the default
 *                             values of pbrMetallicRoughness apply.
 * @param normalTexture        The normal map texture.
 * @param occlusionTexture     The occlusion map texture.
 * @param emissiveTexture      The emissive map texture.
 * @param emissiveFactor       The emissive color of the material.
 * @param alphaMode            The alpha rendering mode of the material.
 * @param alphaCutoff          The alpha cutoff value of the material.
 * @param doubleSided          Specifies whether the material is double sided.
 */
@Serializable
data class GltfMaterial(
    val name: String? = null,
    val pbrMetallicRoughness: PbrMetallicRoughness = PbrMetallicRoughness(
        baseColorFactor = listOf(
            0.5f,
            0.5f,
            0.5f,
            1f
        )
    ),
    val normalTexture: GltfTextureInfo? = null,
    val occlusionTexture: GltfTextureInfo? = null,
    val emissiveTexture: GltfTextureInfo? = null,
    val emissiveFactor: List<Float>? = null,
    val alphaMode: String = ALPHA_MODE_OPAQUE,
    val alphaCutoff: Float = 0.5f,
    val doubleSided: Boolean = false,
) {

    fun toMaterial(file: GltfFile, location: ResourceLocation) = Material().apply {
        val colorList = pbrMetallicRoughness.baseColorFactor
        this.color = Vector4f(colorList[0], colorList[1], colorList[2], colorList[3])

        pbrMetallicRoughness.baseColorTexture?.let {
            this.texture = it.getTexture(file, location)
        }
        this@GltfMaterial.normalTexture?.let {
            this.normalTexture = it.getTexture(file, location)
        }
        pbrMetallicRoughness.metallicRoughnessTexture?.let {
            this.specularTexture = it.getTexture(file, location)
        }
        this.doubleSided = this@GltfMaterial.doubleSided
    }

    /**
     * A set of parameter values that are used to define the metallic-roughness material model from Physically-Based
     * Rendering (PBR) methodology.
     *
     * @param baseColorFactor          The material's base color factor.
     * @param baseColorTexture         The base color texture.
     * @param metallicFactor           The metalness of the material.
     * @param roughnessFactor          The roughness of the material.
     * @param metallicRoughnessTexture The metallic-roughness texture.
     */
    @Serializable
    data class PbrMetallicRoughness(
        val baseColorFactor: List<Float> = listOf(1f, 1f, 1f, 1f),
        val baseColorTexture: GltfTextureInfo? = null,
        val metallicFactor: Float = 1f,
        val roughnessFactor: Float = 1f,
        val metallicRoughnessTexture: GltfTextureInfo? = null,
    )

    companion object {
        const val ALPHA_MODE_BLEND = "BLEND"
        const val ALPHA_MODE_MASK = "MASK"
        const val ALPHA_MODE_OPAQUE = "OPAQUE"
    }
}