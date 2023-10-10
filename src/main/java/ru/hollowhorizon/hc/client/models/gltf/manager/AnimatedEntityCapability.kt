package ru.hollowhorizon.hc.client.models.gltf.manager

import com.mojang.math.Matrix4f
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.utils.nbt.ForMatrix4f
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(IAnimated::class)
class AnimatedEntityCapability : CapabilityInstance() {
    var model by syncable("hc:models/entity/hilda_regular.glb")
    var transform by syncable(Transform())
    val customAnimations by syncableMap<AnimationType, String>()
}

@Serializable
class ModelData(
    val model: String,
    val transform: @Serializable(ForMatrix4f::class) Matrix4f,
    val animations: HashMap<String, String>,
)