package ru.hollowhorizon.hc.common.effects

import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.common.network.packets.S2CAddParticle

object ParticleHelper {

    @JvmStatic
    fun addParticle(level: Level, info: ParticleEmitterInfo, force: Boolean = false) {
        addParticle(level, if (force) 512.0 else 32.0, info)
    }

    @JvmStatic
    fun addParticle(level: Level, distance: Double, info: ParticleEmitterInfo) {
        if (level.isClientSide()) {
            val player = Minecraft.getInstance().player
            if (player?.level !== level) return
            info.spawnInWorld(level, player)
        } else {
            val packet = S2CAddParticle(info)
            val serverLevel = (level as ServerLevel)
            val sqrDistance = distance * distance
            for (player in serverLevel.players()) {
                sendToPlayer(player, serverLevel, packet, sqrDistance)
            }
        }
    }

    private fun sendToPlayer(player: ServerPlayer, level: Level, packet: S2CAddParticle, sqrDistance: Double) {
        if (player.level !== level) return
        if (packet.hasPosition() && player.position().distanceToSqr(packet.position()) > sqrDistance) return

        packet.send(PacketDistributor.PLAYER.with { player })
    }
}
