package ru.hollowhorizon.hc.common.network

import com.google.common.reflect.TypeToken
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import java.util.*
import java.util.function.Supplier


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class HollowPacketV2(val toTarget: NetworkDirection = NetworkDirection.PLAY_TO_CLIENT)

@Suppress("UnstableApiUsage")
open class Packet<T>(val function: Packet<T>.(Player, T) -> Unit) {
    var direction: Optional<NetworkDirection> = Optional.empty()
    var value: T? = null
    var type: TypeToken<T> = object : TypeToken<T>(javaClass) {}

    open fun <E> encode(data: Packet<E>, buf: FriendlyByteBuf) {
        if (data.value == null) throw IllegalStateException("Packet(${data::class.java}) value is null!")

        buf.writeNbt(CompoundTag().apply {
            put(
                "data",
                NBTFormat.serializeNoInline(JavaHacks.forceCast(data.value), this@Packet.type.rawType)
            )
        })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <E> decode(buf: FriendlyByteBuf): Packet<E> {
        val data = NBTFormat.deserializeNoInline(buf.readNbt()!!.get("data")!!, type.rawType)
        this.value = data.safeCast()
        return this as Packet<E>
    }

    fun <E> onReceive(data: Packet<E>, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().packetHandled = true

        ctx.get().enqueueWork {
            HollowCore.LOGGER.info("Packet <${data.javaClass.simpleName}> received (${ctx.get().direction}) (${FMLEnvironment.dist})")

            if (ctx.get().direction == NetworkDirection.PLAY_TO_CLIENT) function(mc.player!!, data.value.safeCast()!!)
            else function(ctx.get().sender!!, data.value.safeCast()!!)
            HollowCore.LOGGER.info("Packet <${data.javaClass.simpleName}> processed")
        }
    }
}

fun <T> Packet<T>.send(data: T, vararg players: Player) {
    this.value = data
    if (players.isEmpty()) {
        NetworkHandler.sendMessageToServer(this)
    } else {
        players.forEach {
            NetworkHandler.sendMessageToClient(this, it)
        }
    }
}

fun <T> Packet<T>.send(data: T, distributor: PacketDistributor.PacketTarget) {
    this.value = data
    NetworkHandler.HollowCoreChannel.send(distributor, this)
}

fun Packet<*>.toVanillaPacket(): net.minecraft.network.protocol.Packet<*> =
    NetworkHandler.HollowCoreChannel.toVanillaPacket(this, NetworkDirection.PLAY_TO_CLIENT)

@Suppress("UNCHECKED_CAST")
fun <T, T1> T.safeCast(): T1? {
    return this as? T1
}