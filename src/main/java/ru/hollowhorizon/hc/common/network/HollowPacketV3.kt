package ru.hollowhorizon.hc.common.network

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.PacketDistributor
import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import java.util.function.Supplier

interface HollowPacketV3<T> {
    fun handle(player: Player, data: T)

    fun send(dist: PacketDistributor.PacketTarget) {
        NetworkHandler.HollowCoreChannel.send(dist, this)
    }

    fun send() {
        NetworkHandler.sendMessageToServer(this)
    }
}

fun <T> Class<T>.register() = JavaHacks.registerPacket(this)

fun <T : HollowPacketV3<T>> registerPacket(packetClass: Class<T>) {
    NetworkHandler.PACKET_TASKS.add {
        NetworkHandler.HollowCoreChannel.registerMessage(
            NetworkHandler.PACKET_INDEX++,
            packetClass,
            { packet: T, buffer: FriendlyByteBuf ->
                val tag = NBTFormat.serializeNoInline(packet, packetClass)
                if (tag is CompoundTag) buffer.writeNbt(tag)
                else buffer.writeNbt(CompoundTag().apply { put("%%data", tag) })
            },
            { buffer: FriendlyByteBuf ->
                val tag = buffer.readNbt() ?: throw IllegalStateException("Can't decode ${packetClass.name} packet!")
                if (tag.contains("%%data")) NBTFormat.deserializeNoInline(tag.get("%%data")!!, packetClass)
                else NBTFormat.deserializeNoInline(tag, packetClass)
            },
            { packet: T, ctx: Supplier<NetworkEvent.Context> ->
                ctx.get().apply {
                    packetHandled = true
                    enqueueWork {
                        if (direction == NetworkDirection.PLAY_TO_CLIENT) packet.handle(mc.player!!, packet)
                        else packet.handle(sender!!, packet)
                    }
                }
            }
        )
    }
}