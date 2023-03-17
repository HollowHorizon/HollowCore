package ru.hollowhorizon.hc.common.network.messages

import com.google.common.reflect.TypeToken
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater
import ru.hollowhorizon.hc.common.capabilities.IHollowCapability
import ru.hollowhorizon.hc.common.capabilities.serialize
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet

class SyncCapabilityEntityPacket<T : IHollowCapability> : Packet<T>({ player, capability ->
    val cap = HollowCapabilityV2.get(capability.javaClass)

    val entityId = (this as SyncCapabilityEntityPacket<*>).entityId

    val mob = player.level.getEntity(entityId)!!
    val uploader = mob as ICapabilityUpdater

    HollowCore.LOGGER.info("Updating EntityCapability: {}, {}", uploader, capability.serialize().asString)

    uploader.updateCapability(cap, capability.serialize())
}) {
    var entityId: Int = -1

    override fun <E> encode(data: Packet<E>, buf: PacketBuffer) {
        val type = TypeToken.of(data.value!!::class.java).rawType
        buf.writeInt((data as SyncCapabilityEntityPacket<*>).entityId)
        buf.writeUtf(type.name)
        buf.writeNbt(
            NBTFormat.serializeNoInline(
                data.value!!,
                type.cast()
            ) as CompoundNBT
        )
    }

    @Suppress("unchecked_cast")
    override fun <E> decode(buf: PacketBuffer): Packet<E> {
        this.entityId = buf.readInt()
        val clazz = Class.forName(buf.readUtf())
        this.value = NBTFormat.deserializeNoInline(buf.readNbt()!!, clazz) as T
        return this.cast()
    }
}