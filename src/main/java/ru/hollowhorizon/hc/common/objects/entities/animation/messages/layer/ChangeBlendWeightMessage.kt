package ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer

import net.minecraft.network.PacketBuffer
import ru.hollowhorizon.hc.common.network.messages.layerMessageDeserializer

class ChangeBlendWeightMessage(val blendWeight: Float) : AnimationLayerMessage(CHANGE_BLEND_WEIGHT_TYPE) {

    override fun toPacketBuffer(buffer: PacketBuffer) {
        super.toPacketBuffer(buffer)
        buffer.writeFloat(blendWeight)
    }

    companion object {
        var CHANGE_BLEND_WEIGHT_TYPE = "CHANGE_BLEND_WEIGHT_TYPE"

        init {
            layerMessageDeserializer.addNetworkDeserializer(CHANGE_BLEND_WEIGHT_TYPE) { buffer: PacketBuffer ->
                fromPacketBuffer(buffer)
            }
        }

        private fun fromPacketBuffer(buffer: PacketBuffer): ChangeBlendWeightMessage {
            val blendWeight = buffer.readFloat()
            return ChangeBlendWeightMessage(blendWeight)
        }
    }
}