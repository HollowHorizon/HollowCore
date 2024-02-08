package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import ru.hollowhorizon.hc.client.utils.toSTC
import ru.hollowhorizon.hc.common.ui.Widget


class UIScreen(val gui: Widget) : HollowScreen("".toSTC()) {
    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        gui.render(pPoseStack, 0, 0, width, height, width, height, pMouseX, pMouseY, pPartialTick)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        return gui.buttonPressed(0, 0, width, height, width, height, pMouseX.toInt(), pMouseY.toInt())
    }
}