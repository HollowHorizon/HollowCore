package ru.hollowhorizon.hc.common.objects.entities.animation.messages

import net.minecraft.network.PacketBuffer
import ru.hollowhorizon.hc.common.network.messages.animationMessageDeserializer
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent

class PushStateMessage(val stateName: String) : AnimationMessage(PUSH_STATE) {

    override fun toPacketBuffer(buffer: PacketBuffer) {
        super.toPacketBuffer(buffer)
        buffer.writeUtf(stateName)
    }

    companion object {
        const val PUSH_STATE = "PUSH_STATE"

        init {
            animationMessageDeserializer.addNetworkDeserializer(PUSH_STATE) { buffer: PacketBuffer ->
                fromPacketBuffer(buffer)
            }
            AnimationComponent.addMessageHandler(PUSH_STATE) { component: AnimationComponent<*>, message: AnimationMessage ->
                handleMessage(component, message)
            }
        }

        private fun handleMessage(component: AnimationComponent<*>, message: AnimationMessage) {
            if (message is PushStateMessage) {
                component.pushState(message.stateName)
            }
        }

        private fun fromPacketBuffer(buffer: PacketBuffer): PushStateMessage {
            val stateName: String = buffer.readUtf()
            return PushStateMessage(stateName)
        }
    }
}