package ru.hollowhorizon.hc.common.network.packets

import kotlinx.serialization.Serializable
import net.minecraft.world.entity.player.Player
import net.minecraftforge.network.NetworkDirection
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationState
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimationLayer
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.network.Packet

@Serializable
data class StopAnimationContainer(
    val entityId: Int, val name: String
)

@HollowPacketV2
@Serializable
class StartAnimationPacket(
    private val entityId: Int,
    private val name: String,
    private val layerMode: LayerMode,
    private val playType: PlayMode,
    private val speed: Float = 1.0f,
) : HollowPacketV3<StartAnimationPacket> {
    override fun handle(player: Player, data: StartAnimationPacket) {
        player.level.getEntity(data.entityId)?.let { entity ->
            if (entity is IAnimated) {
                val capability = entity[AnimatedEntityCapability::class]
                if (capability.layers.any { it.animation == data.name }) return@let

                capability.layers += AnimationLayer(
                    data.name,
                    data.layerMode,
                    data.playType,
                    data.speed,
                    0
                )
            }
        }
    }

}

@HollowPacketV2
@Serializable
class StopAnimationPacket(
    val entityId: Int,
    val name: String
) : HollowPacketV3<StopAnimationPacket> {
    override fun handle(player: Player, data: StopAnimationPacket) {
        player.level.getEntity(data.entityId)?.let { entity ->
            if (entity is IAnimated) {
                val capability = entity[AnimatedEntityCapability::class]

                capability.layers.filter { it.animation == data.name }.forEach {
                    it.state = AnimationState.FINISHED
                }
            }
        }
    }

}