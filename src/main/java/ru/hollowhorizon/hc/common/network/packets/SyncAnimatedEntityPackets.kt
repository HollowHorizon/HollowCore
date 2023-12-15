package ru.hollowhorizon.hc.common.network.packets

import kotlinx.serialization.Serializable
import net.minecraftforge.network.NetworkDirection
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationState
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimationLayer
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet

@Serializable
data class StartAnimationContainer(
    val entityId: Int,
    val name: String,
    val layerMode: LayerMode,
    val playType: PlayMode,
    val speed: Float = 1.0f,
)
@Serializable
data class StopAnimationContainer(
    val entityId: Int, val name: String
)

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_CLIENT)
class StartAnimationPacket : Packet<StartAnimationContainer>({ player, container ->
    player.level.getEntity(container.entityId)?.let { entity ->
        if (entity is IAnimated) {
            val capability = entity[AnimatedEntityCapability::class]
            if (capability.layers.any { it.animation == container.name }) return@let

            capability.layers += AnimationLayer(
                container.name,
                container.layerMode,
                container.playType,
                container.speed,
                0
            )
        }
    }
})

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_CLIENT)
class StopAnimationPacket : Packet<StopAnimationContainer>({ player, container ->
    player.level.getEntity(container.entityId)?.let { entity ->
        if (entity is IAnimated) {
            val capability = entity[AnimatedEntityCapability::class]

            capability.layers.filter { it.animation == container.name }.forEach {
                it.state = AnimationState.FINISHED
            }
        }
    }
})