package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A set of primitives to be rendered. A node can contain one mesh. A node's transform places the mesh in the scene.
 *
 * @param primitives An array of primitives, each defining geometry to be rendered with a material.
 * @param weights    Array of weights to be applied to the Morph Targets.
 * @param name       The user-defined name of this object.
 */
@Serializable
data class GltfMesh(
    val primitives: List<Primitive>,
    val weights: List<Float>? = null,
    val name: String? = null,
) {

    /**
     * Geometry to be rendered with the given material.
     *
     * @param attributes A dictionary object, where each key corresponds to mesh attribute semantic and each value is
     *                   the index of the accessor containing attribute's data.
     * @param indices    The index of the accessor that contains the indices.
     * @param material   The index of the material to apply to this primitive when rendering.
     * @param mode       The type of primitives to render.
     * @param targets    An array of Morph Targets, each Morph Target is a dictionary mapping attributes (only
     *                   POSITION, NORMAL, and TANGENT supported) to their deviations in the Morph Target.
     */
    @Serializable
    data class Primitive(
        val attributes: Map<String, Int>,
        val indices: Int = -1,
        val material: Int = -1,
        val mode: Int = MODE_TRIANGLES,
        val targets: List<Map<String, Int>> = emptyList(),
    ) {
        @Transient
        var materialRef: GltfMaterial? = null

        companion object {
            const val MODE_POINTS = 0
            const val MODE_LINES = 1
            const val MODE_LINE_LOOP = 2
            const val MODE_LINE_STRIP = 3
            const val MODE_TRIANGLES = 4
            const val MODE_TRIANGLE_STRIP = 5
            const val MODE_TRIANGLE_FAN = 6
            const val MODE_QUADS = 7
            const val MODE_QUAD_STRIP = 8
            const val MODE_POLYGON = 9

            const val ATTRIBUTE_POSITION = "POSITION"
            const val ATTRIBUTE_NORMAL = "NORMAL"
            const val ATTRIBUTE_TANGENT = "TANGENT"
            const val ATTRIBUTE_TEXCOORD_0 = "TEXCOORD_0"
            const val ATTRIBUTE_TEXCOORD_1 = "TEXCOORD_1"
            const val ATTRIBUTE_COLOR_0 = "COLOR_0"
            const val ATTRIBUTE_JOINTS_0 = "JOINTS_0"
            const val ATTRIBUTE_WEIGHTS_0 = "WEIGHTS_0"
        }
    }
}