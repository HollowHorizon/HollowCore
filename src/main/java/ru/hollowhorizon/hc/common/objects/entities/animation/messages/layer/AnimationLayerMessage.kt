package ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer

import net.minecraft.network.PacketBuffer


abstract class AnimationLayerMessage(val messageType: String) {

    open fun toPacketBuffer(buffer: PacketBuffer) {
        buffer.writeUtf(messageType)
    }
}
