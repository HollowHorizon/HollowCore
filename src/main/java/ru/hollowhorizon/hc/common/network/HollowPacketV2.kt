package ru.hollowhorizon.hc.common.network

import com.google.common.reflect.TypeToken
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkEvent
import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.nbt.*
import ru.hollowhorizon.hc.client.utils.toSTC
import java.util.*
import java.util.function.Supplier


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class HollowPacketV2(val toTarget: Dist = Dist.DEDICATED_SERVER)

open class Packet<T>(val function: Packet<T>.(PlayerEntity, T) -> Unit) {
    var direction: Optional<NetworkDirection> = Optional.empty()
    var value: T? = null
    var typeToken: TypeToken<T> = object : TypeToken<T>(javaClass) {}

    constructor(function: Packet<T>.(PlayerEntity, T) -> Unit, token: Class<T>) : this(function) {
        typeToken = TypeToken.of(token)
    }

    fun send(data: T, vararg players: PlayerEntity) {
        this.value = data
        if (players.isEmpty()) {
            NetworkHandler.sendMessageToServer(this)
        } else {
            players.forEach {
                NetworkHandler.sendMessageToClient(this, it)
            }
        }
    }

    open fun <E> encode(data: Packet<E>, buf: PacketBuffer) {
        if (data.value == null) throw IllegalStateException("Packet(${data::class.java}) value is null!")

        buf.writeNbt(CompoundNBT().apply {
            put(
                "data",
                NBTFormat.serializeNoInline(HollowJavaUtils.castDarkMagic(data.value), data.value!!::class.java)
            )
        })
    }

    open fun <E> decode(buf: PacketBuffer): Packet<E> {
        val data = NBTFormat.deserializeNoInline(buf.readNbt()!!.get("data")!!, typeToken.rawType)
        this.value = data.safeCast()
        return this.cast()
    }

    fun <E> onReceive(data: Packet<E>, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().packetHandled = true
        if (FMLEnvironment.dist.isClient) function(mc.player!!, data.value.safeCast()!!)
        else function(ctx.get().sender!!, data.value.safeCast()!!)

    }
}

fun Packet<*>.toVanillaPacket(): IPacket<*> =
    NetworkHandler.HollowCoreChannel.toVanillaPacket(this, NetworkDirection.PLAY_TO_CLIENT)

@Suppress("UNCHECKED_CAST")
fun <T, T1> T.safeCast(): T1? {
    return this as? T1
}

@Serializable
data class TestData(val a: Int, val b: String)

@HollowPacketV2(Dist.CLIENT)
class ExamplePacket : Packet<String>({ player, data ->
    player.sendMessage(data.toSTC(), player.uuid)
})