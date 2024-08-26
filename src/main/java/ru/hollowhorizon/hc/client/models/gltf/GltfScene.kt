package ru.hollowhorizon.hc.client.models.gltf

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GltfScene(
    val nodes: List<Int>,
    val name: String? = null
) {
    @Transient
    lateinit var nodeRefs: List<GltfNode>
}