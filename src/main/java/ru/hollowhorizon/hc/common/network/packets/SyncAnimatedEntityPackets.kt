package ru.hollowhorizon.hc.common.network.packets

import kotlinx.serialization.Serializable
import net.minecraftforge.network.NetworkDirection
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayType
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimationLayer
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet

@Serializable
data class StartAnimationContainer(
    val entityId: Int,
    val name: String,
    val priority: Float = 1.0f,
    val speed: Float = 1.0f,
)

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_CLIENT)
class StartOnceAnimationPacket : Packet<StartAnimationContainer>({ player, container ->
    player.level.getEntity(container.entityId)?.let { entity ->
        if (entity is IAnimated) {
            val capability = entity[AnimatedEntityCapability::class]
            capability.onceAnimations += AnimationLayer(
                container.name, container.priority, PlayType.ONCE, container.speed, 0
            )
        }
    }
})