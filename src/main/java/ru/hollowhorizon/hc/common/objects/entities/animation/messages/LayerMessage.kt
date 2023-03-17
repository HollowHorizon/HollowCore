package ru.hollowhorizon.hc.common.objects.entities.animation.messages

import net.minecraft.network.PacketBuffer
import ru.hollowhorizon.hc.common.network.messages.animationMessageDeserializer
import ru.hollowhorizon.hc.common.network.messages.layerMessageDeserializer
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage

class LayerMessage(stateName: String, layerName: String, val message: AnimationLayerMessage) :
    LayerControlMessage(LAYER_MESSAGE, stateName, layerName) {

    override fun toPacketBuffer(buffer: PacketBuffer) {
        super.toPacketBuffer(buffer)
        message.toPacketBuffer(buffer)
    }

    companion object {
        const val LAYER_MESSAGE = "LAYER_MESSAGE"

        init {
            animationMessageDeserializer.addNetworkDeserializer(LAYER_MESSAGE) { buffer: PacketBuffer ->
                fromPacketBuffer(buffer)
            }
            AnimationComponent.addMessageHandler(LAYER_MESSAGE) { component: AnimationComponent<*>, message: AnimationMessage ->
                handleMessage(component, message)
            }
        }

        private fun handleMessage(component: AnimationComponent<*>, msg: AnimationMessage) {
            if (msg is LayerMessage) {
                    component.distributeLayerMessage(msg.stateName, msg.layerName, msg.message)

            }
        }

        private fun fromPacketBuffer(buffer: PacketBuffer): LayerMessage {
            val stateName: String = buffer.readUtf()
            val layerName: String = buffer.readUtf()
            val msg: AnimationLayerMessage = layerMessageDeserializer.deserialize(buffer) ?: throw IllegalStateException("LayerMessage deserializer returned null")
            return LayerMessage(stateName, layerName, msg)
        }
    }
}
