package ru.hollowhorizon.hc.particles.api.common

import ru.hollowhorizon.hc.particles.client.EffekseerParticlesClient
import ru.hollowhorizon.hc.particles.common.network.S2CAddParticle
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraftforge.network.PacketDistributor

object ParticleHelper {
    @JvmStatic
    fun addParticle(level: Level, info: ParticleEmitterInfo) {
        addParticle(level, false, info)
    }

    @JvmStatic
    fun addParticle(level: Level, force: Boolean, info: ParticleEmitterInfo) {
        addParticle(level, if (force) 512.0 else 32.0, info)
    }

    @JvmStatic
    fun addParticle(level: Level, distance: Double, info: ParticleEmitterInfo) {
        if (level.isClientSide()) {
            EffekseerParticlesClient.addParticle(level, info)
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
