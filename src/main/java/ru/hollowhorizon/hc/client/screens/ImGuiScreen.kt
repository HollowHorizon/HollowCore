package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.Blaze3D
import imgui.ImGui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.client.imgui.Renderable
import kotlin.math.sin

open class ImGuiScreen(private val drawer: Renderable = Renderable { example() }) : Screen(Component.empty()) {
    override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(
            gui
            //? if >=1.21 {
            , mouseX, mouseY, partialTick
            //?}
        )
        ImGuiHandler.drawFrame(drawer)
    }
}

fun Graphics.example() {
    withFontSize(100) {
        val data = "Привет, вот тут какой-то там текст"
        val scale = ((sin(Blaze3D.getTime()) + 1f) / 2f * 150 % 150).toInt() + 1
        val size = ImGui.calcTextSize(data).times(scale / 100f, scale / 100f)
        ImGui.getWindowDrawList()
            .textShadow(screenWidth / 2f - size.x / 2f, screenHeight / 2f - size.y / 2f, data, size = scale)
    }
}