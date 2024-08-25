package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable

/**
 * Texture sampler properties for filtering and wrapping modes.
 *
 * @param magFilter Magnification filter.
 * @param minFilter Minification filter.
 * @param wrapS     S (U) wrapping mode.
 * @param wrapT     T (V) wrapping mode.
 * @param name      The user-defined name of this object.
 */
@Serializable
data class GltfSampler(
    val magFilter: Int = LINEAR,
    val minFilter: Int = LINEAR,
    val wrapS: Int = REPEAT,
    val wrapT: Int = REPEAT,
    val name: String? = null
) {

    companion object {
        const val NEAREST = 9728
        const val LINEAR = 9729
        const val NEAREST_MIPMAP_NEAREST = 9984
        const val LINEAR_MIPMAP_NEAREST = 9985
        const val NEAREST_MIPMAP_LINEAR = 9986
        const val LINEAR_MIPMAP_LINEAR = 9987

        const val CLAMP_TOEDGE = 33071
        const val MIRRORED_REPEAT = 33648
        const val REPEAT = 10497
    }
}