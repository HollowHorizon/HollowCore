package ru.hollowhorizon.hc.common.objects.entities.animation.messages

import net.minecraft.network.PacketBuffer
import ru.hollowhorizon.hc.common.network.messages.animationMessageDeserializer
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent

open class LayerControlMessage(type: String, val stateName: String, val layerName: String) :
    AnimationMessage(type) {

    override fun toPacketBuffer(buffer: PacketBuffer) {
        super.toPacketBuffer(buffer)
        buffer.writeUtf(stateName)
        buffer.writeUtf(layerName)
    }

    companion object {
        const val START_LAYER = "START_LAYER"
        const val STOP_LAYER = "STOP_LAYER"

        init {
            animationMessageDeserializer.addNetworkDeserializer(
                START_LAYER
            ) { buffer: PacketBuffer -> fromPacketBufferStart(buffer) }

            animationMessageDeserializer.addNetworkDeserializer(
                STOP_LAYER
            ) { buffer: PacketBuffer -> fromPacketBufferStop(buffer) }

            AnimationComponent.addMessageHandler(STOP_LAYER) { component: AnimationComponent<*>, message: AnimationMessage ->
                handleMessage(component, message)
            }
            AnimationComponent.addMessageHandler(START_LAYER) { component: AnimationComponent<*>, message: AnimationMessage ->
                handleMessage(component, message)
            }
        }

        private fun handleMessage(component: AnimationComponent<*>, msg: AnimationMessage) {
            if (msg is LayerControlMessage) {
                when (msg.type) {
                    START_LAYER -> component.startLayer(msg.stateName, msg.layerName)
                    STOP_LAYER -> component.stopLayer(msg.stateName, msg.layerName)
                    else -> {}
                }
            }
        }

        private fun fromPacketBufferStart(buffer: PacketBuffer): LayerControlMessage {
            val stateName: String = buffer.readUtf()
            val layerName: String = buffer.readUtf()
            return getStartMessage(stateName, layerName)
        }

        private fun fromPacketBufferStop(buffer: PacketBuffer): LayerControlMessage {
            val stateName: String = buffer.readUtf()
            val layerName: String = buffer.readUtf()
            return getStopMessage(stateName, layerName)
        }

        private fun getStartMessage(stateName: String, layerName: String): LayerControlMessage {
            return LayerControlMessage(START_LAYER, stateName, layerName)
        }

        private fun getStopMessage(stateName: String, layerName: String): LayerControlMessage {
            return LayerControlMessage(STOP_LAYER, stateName, layerName)
        }
    }
}
