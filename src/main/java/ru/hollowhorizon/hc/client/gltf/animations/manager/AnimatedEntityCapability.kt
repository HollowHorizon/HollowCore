package ru.hollowhorizon.hc.client.gltf.animations.manager

import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.Transform
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(IAnimated::class)
class AnimatedEntityCapability : CapabilityInstance() {
    var model by syncable("hc:models/entity/player_model.gltf")
    var transform by syncable(Transform())
    val customAnimations by syncableMap<AnimationType, String>()
}