package ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer

import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.common.network.messages.layerMessageDeserializer
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.LayerWithAnimation

class ChangeLayerAnimationMessage @JvmOverloads constructor(
    val anim: ResourceLocation,
    val slot: String = LayerWithAnimation.BASE_SLOT,
) :
    AnimationLayerMessage(CHANGE_ANIMATION_TYPE) {

    override fun toPacketBuffer(buffer: PacketBuffer) {
        super.toPacketBuffer(buffer)
        buffer.writeUtf(slot)
        buffer.writeResourceLocation(anim)
    }

    companion object {
        var CHANGE_ANIMATION_TYPE = "CHANGE_ANIMATION_TYPE"

        init {
            layerMessageDeserializer.addNetworkDeserializer(CHANGE_ANIMATION_TYPE) { buffer: PacketBuffer ->
                fromPacketBuffer(
                    buffer
                )
            }
        }

        private fun fromPacketBuffer(buffer: PacketBuffer): ChangeLayerAnimationMessage {
            val slot: String = buffer.readUtf()
            val animName = buffer.readResourceLocation()
            return ChangeLayerAnimationMessage(animName, slot)
        }
    }
}
