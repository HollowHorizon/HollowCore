package ru.hollowhorizon.hc.client.models.gltf.manager

import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.client.models.gltf.Transform

@Serializable
data class SubModel(
    var model: String,
    val layers: MutableList<AnimationLayer>,
    val textures: MutableMap<String, String>,
    var transform: Transform,
    val subModels: MutableMap<String, SubModel>,
)