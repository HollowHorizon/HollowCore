package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager

@HollowCapabilityV2(PlayerEntity::class)
@Serializable
class AnimatedEntityCapability : IHollowCapability {
    var manager = GLTFAnimationManager()
}