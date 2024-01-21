package ru.hollowhorizon.hc.client.models.gltf.manager

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(IAnimated::class)
class AnimatedEntityCapability : CapabilityInstance() {
    internal val definedLayer = DefinedLayer()
    internal val headLayer = HeadLayer()
    var model by syncable("%NO_MODEL%")
    val layers by syncableList<AnimationLayer>()
    val textures by syncableMap<String, String>()
    val animations by syncableMap<AnimationType, String>()
    var transform by syncable(Transform())
    val subModels by syncableMap<String, SubModel>()
    var switchHeadRot by syncable(false)
}
