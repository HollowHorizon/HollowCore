/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.effects

import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.common.network.packets.S2CAddParticle
//? if <=1.19.2
import ru.hollowhorizon.hc.client.utils.math.level

object ParticleHelper {

    @JvmStatic
    fun addParticle(level: Level, info: ParticleEmitterInfo, force: Boolean = false) {
        addParticle(level, if (force) 512.0 else 32.0, info)
    }

    @JvmStatic
    fun addParticle(level: Level, distance: Double, info: ParticleEmitterInfo) {
        if (level.isClientSide()) {
            val player = Minecraft.getInstance().player
            if (player?.level() !== level) return
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
        if (player.level() !== level) return
        if (packet.hasPosition() && player.position().distanceToSqr(packet.position()) > sqrDistance) return

        packet.send(player)
    }
}
