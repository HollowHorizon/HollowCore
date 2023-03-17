package ru.hollowhorizon.hc.common.network.messages

import net.minecraft.network.PacketBuffer
import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.AnimationMessage
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage

@HollowPacketV2
class EntityAnimationClientUpdatePacket : Packet<EntityAnimationClientUpdatePacket>({ player, packet ->
    player.level.getEntity(packet.entityId)?.let { entity ->
        if (entity is IBTAnimatedEntity<*>) {
            packet.messages.forEach { entity.animationComponent.updateState(it) }
        }
    }

}) {
    var entityId: Int = -1
    var messages = ArrayList<AnimationMessage>()

    override fun <E> encode(data: Packet<E>, buf: PacketBuffer) {
        val packet = data.cast<EntityAnimationClientUpdatePacket>()
        buf.writeInt(packet.entityId)
        buf.writeInt(packet.messages.size)
        packet.messages.forEach { it.toPacketBuffer(buf) }
    }

    override fun <E> decode(buf: PacketBuffer): Packet<E> {
        entityId = buf.readInt()
        val size = buf.readInt()
        for (i in 0 until size) {
            val message = animationMessageDeserializer.deserialize(buf)

            if (message == null) {
                HollowCore.LOGGER.error("Error decoding EntityAnimationClientUpdatePacket for Entity: {}", entityId)
                break
            }

            messages.add(message)

        }
        this.value = this
        return this.cast()
    }
}

val animationMessageDeserializer = StringTypeNetworkDeserializer<AnimationMessage>()
val layerMessageDeserializer = StringTypeNetworkDeserializer<AnimationLayerMessage>()

class StringTypeNetworkDeserializer<T> {
    private val deserializers = HashMap<String, (PacketBuffer) -> T>()

    fun addNetworkDeserializer(messageType: String, callback: (PacketBuffer) -> T) {
        deserializers[messageType] = callback
    }

    fun deserialize(message: PacketBuffer): T? {
        val type: String = message.readUtf()
        return deserializers[type]?.invoke(message) ?: run {
            HollowCore.LOGGER.error("Failed to find deserializer for type: {}", type)
            null
        }

    }
}