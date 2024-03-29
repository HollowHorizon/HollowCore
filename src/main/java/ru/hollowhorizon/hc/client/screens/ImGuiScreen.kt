package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.flag.ImGuiBackendFlags
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiKey
import org.lwjgl.glfw.GLFW.*
import ru.hollowhorizon.hc.client.imgui.ImguiLoader
import ru.hollowhorizon.hc.client.imgui.Renderable
import ru.hollowhorizon.hc.client.imgui.test


class ImGuiScreen(private val renderable: Renderable = test()) : HollowScreen() {
    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        ImguiLoader.imGuiGlfw.newFrame()
        ImGui.newFrame()
        ImGui.setNextWindowViewport(ImGui.getMainViewport().id)

        renderable.getTheme()?.preRender()
        renderable.render()
        renderable.getTheme()?.postRender()

        ImGui.render()
        ImguiLoader.endFrame()
    }

    override fun charTyped(pCodePoint: Char, pModifiers: Int): Boolean {
        //ImguiLoader.charTyped(pCodePoint, pModifiers)
        return super.charTyped(pCodePoint, pModifiers)
    }
}