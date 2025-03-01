package ru.hollowhorizon.hc.common.network.packets

import io.netty.buffer.Unpooled
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.LOGGER
import ru.hollowhorizon.hc.common.network.HollowPacketHandler
import ru.hollowhorizon.hc.common.network.HollowPacket
import ru.hollowhorizon.hc.common.objects.recipe.HollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.deep.SupportedIngredientsPacketEncoder

private const val PROTOCOL_VERSION = 4

@Serializable
@HollowPacketHandler(toTarget = HollowPacketHandler.Direction.TO_CLIENT)
class HollowIngredientSync(private val protVersion: Int) : HollowPacket<HollowIngredientSync> {
    override fun handle(player: Player) {
        if (protVersion < PROTOCOL_VERSION) return
        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeVarInt(PROTOCOL_VERSION)
            writeCollection(HollowIngredient.registeredSerializers.keys, FriendlyByteBuf::writeResourceLocation)
        }

        val idSet = when(val protVersion = buf.readVarInt()) {
            PROTOCOL_VERSION -> {
                val ids = buf.readCollection(::HashSet, FriendlyByteBuf::readResourceLocation)
                ids.removeIf { !HollowIngredient.registeredSerializers.containsKey(it) }
                ids
            }
            else -> throw IllegalArgumentException("Unknown ingredient sync protocol version: $protVersion")
        }
        val pe = Minecraft.getInstance().connection?.connection?.channel?.pipeline()?.get("encoder") ?: return
        (pe as SupportedIngredientsPacketEncoder).hcSetSupportedIngredients(idSet)
    }
}
