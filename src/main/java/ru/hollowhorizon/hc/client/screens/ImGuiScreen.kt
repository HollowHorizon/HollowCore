package ru.hollowhorizon.hc.client.screens

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.client.imgui.Renderable

open class ImGuiScreen(private val drawer: Renderable): Screen(Component.empty()) {
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