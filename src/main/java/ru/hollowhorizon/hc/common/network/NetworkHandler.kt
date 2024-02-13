package ru.hollowhorizon.hc.common.network

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel
import ru.hollowhorizon.hc.common.network.packets.SpawnParticlesPacket

object NetworkHandler {
    val HOLLOW_CORE_CHANNEL: ResourceLocation = ResourceLocation("hc", "hollow_core_channel")
    val PACKETS: MutableMap<String, MutableList<Runnable>> = HashMap()
    lateinit var HollowCoreChannel: SimpleChannel
    var PACKET_INDEX: Int = 0

    fun <MSG> sendMessageToClient(messageToClient: MSG, player: Player) {
        HollowCoreChannel.sendTo(
            messageToClient,
            (player as ServerPlayer).connection.connection,
            NetworkDirection.PLAY_TO_CLIENT
        )
    }

    fun <MSG> sendMessageToClientTrackingChunk(msg: MSG, level: Level, pos: BlockPos) {
        HollowCoreChannel.send(PacketDistributor.TRACKING_CHUNK.with { level.getChunkAt(pos) }, msg)
    }

    @OnlyIn(Dist.CLIENT)
    fun <MSG> sendMessageToServer(messageToServer: MSG) {
        if (Minecraft.getInstance().connection == null) return
        HollowCoreChannel.sendToServer(messageToServer)
    }

    @JvmStatic
    fun register() {
        val index = PACKETS.values.sumOf { it.size } + 1

        HollowCoreChannel = NetworkRegistry.newSimpleChannel(
            HOLLOW_CORE_CHANNEL, { index.toString() },
            { it == index.toString() },
            { it == index.toString() }
        )


        //без сортировки он может это сделать в любом порядке, так клиентские и серверные пакеты могут перепутаться
        PACKETS.keys.sorted().forEach { it: String ->
            PACKETS[it]!!.forEach(Runnable::run)
        }

        HollowCoreChannel.registerMessage(
            PACKET_INDEX++,
            SpawnParticlesPacket::class.java,
            SpawnParticlesPacket::write,
            SpawnParticlesPacket::read,
            SpawnParticlesPacket::handle
        )
    }
}
