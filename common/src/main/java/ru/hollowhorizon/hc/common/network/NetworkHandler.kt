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

package ru.hollowhorizon.hc.common.network

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

object NetworkHandler { //TODO: Fix packets
    private val HOLLOW_CORE_CHANNEL: ResourceLocation = ResourceLocation("hc", "hollow_core_channel")
    val PACKETS: MutableMap<String, MutableList<Runnable>> = HashMap()

    //lateinit var HollowCoreChannel: SimpleChannel
    var PACKET_INDEX: Int = 0

    fun <MSG> sendMessageToClient(messageToClient: MSG, player: Player) {
//        HollowCoreChannel.sendTo(
//            messageToClient,
//            (player as ServerPlayer).connection.connection,
//            NetworkDirection.PLAY_TO_CLIENT
//        )
    }

    fun <MSG> sendMessageToClientTrackingChunk(msg: MSG, level: Level, pos: BlockPos) {
        //HollowCoreChannel.send(PacketDistributor.TRACKING_CHUNK.with { level.getChunkAt(pos) }, msg)
    }

    fun <MSG> sendMessageToServer(messageToServer: MSG) {
        if (Minecraft.getInstance().connection == null) return
        //HollowCoreChannel.sendToServer(messageToServer)
    }

    @JvmStatic
    @Suppress("inaccessible_type")
    fun register() {
        val index = PACKETS.values.sumOf { it.size } + 1

//        HollowCoreChannel = NetworkRegistry.newSimpleChannel(
//            HOLLOW_CORE_CHANNEL, { index.toString() },
//            { it == index.toString() },
//            { it == index.toString() }
//        )
//

        //без сортировки он может это сделать в любом порядке, так клиентские и серверные пакеты могут перепутаться
        PACKETS.keys.sorted().forEach { it: String ->
            PACKETS[it]!!.forEach(Runnable::run)
        }

//        HollowCoreChannel.registerMessage(
//            PACKET_INDEX++,
//            SpawnParticlesPacket::class.java,
//            SpawnParticlesPacket::write,
//            SpawnParticlesPacket::read,
//            SpawnParticlesPacket::handle
//        )
    }
}
