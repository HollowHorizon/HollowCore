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

package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hc.client.render.effekseer.loader.EffekAssets
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.effects.ParticleEmitterInfo
import ru.hollowhorizon.hc.common.effects.ParticleHelper
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.RequestPacket

@HollowPacketV2
@Serializable
class ExampleDataPacket(var level: String = "") : RequestPacket<ExampleDataPacket>() {
    override fun retrieveValue(player: ServerPlayer) {
        level = player.serverLevel().dimension().location().toString()
    }
}

object HollowCommands {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.onRegisterCommands {
            "hollowcore" {
                "particle"(
                    arg("pos", Vec3Argument.vec3()),
                    arg("name", StringArgumentType.greedyString(), EffekAssets.entries().map { it.key.toString() })
                ) {
                    val particle = StringArgumentType.getString(this, "name")
                    val pos = Vec3Argument.getVec3(this, "pos")

                    val info = ParticleEmitterInfo(particle.rl).position(pos)
                    ParticleHelper.addParticle(Minecraft.getInstance().level!!, info, true)

                }

            }
        }
    }
}
