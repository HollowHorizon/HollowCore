package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent
import ru.hollowhorizon.hc.api.IAutoScaled
import ru.hollowhorizon.hc.client.utils.toSTC
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.ui.CURRENT_CLIENT_GUI
import ru.hollowhorizon.hc.common.ui.CURRENT_SERVER_GUI
import ru.hollowhorizon.hc.common.ui.Widget


class UIScreen(val gui: Widget) : HollowScreen("".toSTC()), IAutoScaled {

    init {
        Minecraft.getInstance().options.hideGui = true
    }

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        gui.render(pPoseStack, 0, 0, width, height, width, height, pMouseX, pMouseY, pPartialTick)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        return gui.buttonPressed(0, 0, width, height, width, height, pMouseX.toInt(), pMouseY.toInt())
    }

    override fun onClose() {
        super.onClose()
        CURRENT_CLIENT_GUI = null
        Minecraft.getInstance().options.hideGui = false
        ClosedGuiPacket().send()
    }

    override fun isPauseScreen() = false
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class ClosedGuiPacket : HollowPacketV3<ClosedGuiPacket> {
    override fun handle(player: Player, data: ClosedGuiPacket) {
        CURRENT_SERVER_GUI = null
        MinecraftForge.EVENT_BUS.post(ClosedGuiEvent(player))
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CloseGuiPacket : HollowPacketV3<CloseGuiPacket> {
    override fun handle(player: Player, data: CloseGuiPacket) {
        CURRENT_CLIENT_GUI = null
        Minecraft.getInstance().screen?.onClose()
    }

}

class ClosedGuiEvent(player: Player) : PlayerEvent(player)