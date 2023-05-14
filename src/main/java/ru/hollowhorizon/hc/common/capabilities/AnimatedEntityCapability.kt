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
class AnimatedEntityCapability : HollowCapability() {
    var manager = GLTFAnimationManager()
    var model = "hc:models/entity/npc.geo.gltf"
    var animations = HashMap<AnimationTypes, String>()
    var textures = HashMap<String, String>()
    @Serializable(ForMatrix4f::class)
    var transform = Matrix4f().apply {
        setIdentity()
    }
}