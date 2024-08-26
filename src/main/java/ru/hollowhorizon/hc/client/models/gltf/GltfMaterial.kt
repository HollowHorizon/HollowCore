package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.models.internal.Material

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
    val normalTexture: GltfTexture.Info? = null,
    val occlusionTexture: GltfTexture.Info? = null,
    val emissiveTexture: GltfTexture.Info? = null,
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

    @Serializable
    data class PbrMetallicRoughness(
        val baseColorFactor: List<Float> = listOf(1f, 1f, 1f, 1f),
        val baseColorTexture: GltfTexture.Info? = null,
        val metallicFactor: Float = 1f,
        val roughnessFactor: Float = 1f,
        val metallicRoughnessTexture: GltfTexture.Info? = null,
    )

    companion object {
        const val ALPHA_MODE_BLEND = "BLEND"
        const val ALPHA_MODE_MASK = "MASK"
        const val ALPHA_MODE_OPAQUE = "OPAQUE"
    }
}