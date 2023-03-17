package ru.hollowhorizon.hc.common.objects.entities.animation.messages

import net.minecraft.network.PacketBuffer
import ru.hollowhorizon.hc.common.network.messages.animationMessageDeserializer
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent

class PopStateMessage : AnimationMessage(POP_STATE) {

    companion object {
        const val POP_STATE = "POP_STATE"

        init {
            animationMessageDeserializer.addNetworkDeserializer(POP_STATE) { buffer: PacketBuffer ->
                fromPacketBuffer(buffer)
            }
            AnimationComponent.addMessageHandler(POP_STATE) { component: AnimationComponent<*>, message: AnimationMessage ->
                handleMessage(component, message)
            }
        }

        private fun handleMessage(component: AnimationComponent<*>, message: AnimationMessage) {
            component.popState()
        }

        private fun fromPacketBuffer(buffer: PacketBuffer): PopStateMessage {
            return PopStateMessage()
        }
    }
}