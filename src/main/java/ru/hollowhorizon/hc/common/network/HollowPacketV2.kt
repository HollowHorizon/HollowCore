package ru.hollowhorizon.hc.common.network

import com.google.common.reflect.TypeToken
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.PacketDistributor
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.nbt.CAPABILITY_SERIALIZER
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.common.capabilities.HollowCapability
import java.util.*
import java.util.function.Supplier


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class HollowPacketV2(val toTarget: Dist = Dist.DEDICATED_SERVER)

@Suppress("UnstableApiUsage")
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

    fun send(data: T, distributor: PacketDistributor.PacketTarget) {
        this.value = data
        NetworkHandler.HollowCoreChannel.send(distributor, this)
    }

    open fun <E> encode(data: Packet<E>, buf: PacketBuffer) {
        if (data.value == null) throw IllegalStateException("Packet(${data::class.java}) value is null!")

        val serializer = if (data.value is HollowCapability) CAPABILITY_SERIALIZER else NBTFormat

        buf.writeNbt(CompoundNBT().apply {
            put(
                "data",
                serializer.serializeNoInline(HollowJavaUtils.castDarkMagic(data.value), typeToken.rawType)
            )
        })
    }

    @Suppress("UNCHECKED_CAST")
    open fun <E> decode(buf: PacketBuffer): Packet<E> {
        val serializer =
            if (typeToken.rawType.isAssignableFrom(HollowCapability::class.java)) CAPABILITY_SERIALIZER else NBTFormat
        val data = serializer.deserializeNoInline(buf.readNbt()!!.get("data")!!, typeToken.rawType)
        this.value = data.safeCast()
        return this as Packet<E>
    }

    fun <E> onReceive(data: Packet<E>, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().packetHandled = true

        HollowCore.LOGGER.info("Packet <${data.javaClass.simpleName}> received (${ctx.get().direction}) (${FMLEnvironment.dist})")

        if (ctx.get().direction == NetworkDirection.PLAY_TO_CLIENT) function(mc.player!!, data.value.safeCast()!!)
        else function(ctx.get().sender!!, data.value.safeCast()!!)

    }
}

fun Packet<*>.toVanillaPacket(): IPacket<*> =
    NetworkHandler.HollowCoreChannel.toVanillaPacket(this, NetworkDirection.PLAY_TO_CLIENT)

@Suppress("UNCHECKED_CAST")
fun <T, T1> T.safeCast(): T1? {
    return this as? T1
}