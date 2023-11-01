package ru.hollowhorizon.hc.client.models.gltf.manager

import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(IAnimated::class)
class AnimatedEntityCapability : CapabilityInstance() {
    internal val definedLayer = DefinedLayer()
    internal val headLayer = HeadLayer()
    internal val onceAnimations = ArrayList<AnimationLayer>()
    val layers by syncableList<AnimationLayer>()
    var model by syncable("%NO_MODEL%")
    var transform by syncable(Transform())
}