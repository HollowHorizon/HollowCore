package ru.hollowhorizon.hc.common.network.packets

import kotlinx.serialization.Serializable
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.effects.ParticleEmitterInfo

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class S2CAddParticle(private val info: ParticleEmitterInfo) : HollowPacketV3<S2CAddParticle> {

    fun hasPosition() = info.hasPosition

    fun position() = info.position()

    override fun handle(player: Player, data: S2CAddParticle) {
        data.info.spawnInWorld(player.level, player)
    }
}
