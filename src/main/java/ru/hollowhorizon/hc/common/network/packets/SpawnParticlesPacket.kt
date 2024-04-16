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

package ru.hollowhorizon.hc.common.network.packets

import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.Supplier

class SpawnParticlesPacket(
    val options: ParticleOptions,
    val spawnX: Double,
    val spawnY: Double,
    val spawnZ: Double,
    val moveX: Double,
    val moveY: Double,
    val moveZ: Double
) {
    fun write(buf: FriendlyByteBuf) {
        buf.writeRegistryId(ForgeRegistries.PARTICLE_TYPES, options.type)
        options.writeToNetwork(buf)
        buf.writeDouble(this.spawnX)
        buf.writeDouble(this.spawnY)
        buf.writeDouble(this.spawnZ)
        buf.writeDouble(this.moveX)
        buf.writeDouble(this.moveY)
        buf.writeDouble(this.moveZ)
    }

    companion object {
        @JvmStatic
        fun read(buf: FriendlyByteBuf): SpawnParticlesPacket {
            val type = buf.readRegistryId<ParticleType<ParticleOptions>>()
            return SpawnParticlesPacket(
                type.deserializer.fromNetwork(type, buf),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble()
            )
        }

        @JvmStatic
        fun handle(data: SpawnParticlesPacket, ctx: Supplier<NetworkEvent.Context>) {
            ctx.get().apply {
                packetHandled = true
                enqueueWork {
                    val world = Minecraft.getInstance().level

                    world?.addParticle(
                        data.options,
                        true,
                        data.spawnX, data.spawnY, data.spawnZ,
                        data.moveX, data.moveY, data.moveZ
                    )
                }
            }
        }
    }
}