package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Vector3f
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.animation.AnimationTypes
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.client.utils.nbt.ForMatrix4f

@HollowCapabilityV2(IAnimatedEntity::class)
@Serializable
class AnimatedEntityCapability : HollowCapability(true) {
    var manager = GLTFAnimationManager()
    var model = "hc:models/entity/npc.geo.gltf"
    var animations = HashMap<AnimationTypes, String>()
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