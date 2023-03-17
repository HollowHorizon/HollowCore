package ru.hollowhorizon.hc.common.objects.entities.animation.messages

import net.minecraft.network.PacketBuffer

abstract class AnimationMessage(val type: String) {
    open fun toPacketBuffer(buffer: PacketBuffer) {
        buffer.writeUtf(type)
    }

}
