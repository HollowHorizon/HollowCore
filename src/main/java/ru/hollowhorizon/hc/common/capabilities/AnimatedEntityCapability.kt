package ru.hollowhorizon.hc.common.capabilities

import com.mojang.math.Vector3f
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType

//@HollowCapabilityV2(IAnimatedEntity::class)
@Serializable
class AnimatedEntityCapability : HollowCapability() {
    val animationsToStart = HashSet<String>()
    val animationsToStop = HashSet<String>()
    var model = "hc:models/entity/npc.geo.gltf"
    var animations = HashMap<AnimationType, String>()
    var textures = HashMap<String, String>()
    var transform = Transform()
}

@Serializable
data class Transform(
    var tX: Float = 0f, var tY: Float = 0f, var tZ: Float = 0f,
    var rX: Float = 0f, var rY: Float = 0f, var rZ: Float = 0f,
    var sX: Float = 1.0f, var sY: Float = 1.0f, var sZ: Float = 1.0f,
) {
    fun vecTransform() = Vector3f(tX, tY, tZ)
}

