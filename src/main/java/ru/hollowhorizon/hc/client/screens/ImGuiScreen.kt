package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import ru.hollowhorizon.hc.client.imgui.ImguiHandler

import ru.hollowhorizon.hc.client.imgui.Renderable
import ru.hollowhorizon.hc.client.imgui.test


class ImGuiScreen(private val renderable: Renderable = test()) : HollowScreen() {
    var mouseClicked = false
    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        ImguiHandler.drawFrame(renderable)
    }

    override fun charTyped(pCodePoint: Char, pModifiers: Int): Boolean {
        //ImguiLoader.charTyped(pCodePoint, pModifiers)
        return super.charTyped(pCodePoint, pModifiers)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        mouseClicked = true
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        mouseClicked = false
        return super.mouseReleased(pMouseX, pMouseY, pButton)
    }
}