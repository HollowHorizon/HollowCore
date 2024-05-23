/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent
import ru.hollowhorizon.hc.api.AutoScaled
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.ui.CURRENT_CLIENT_GUI
import ru.hollowhorizon.hc.common.ui.CURRENT_SERVER_GUI
import ru.hollowhorizon.hc.common.ui.Widget


class UIScreen(val gui: Widget) : HollowScreen("".mcText), AutoScaled {

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