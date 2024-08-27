package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.Blaze3D
import imgui.ImGui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.client.imgui.Renderable
import kotlin.math.sin

open class ImGuiScreen(private val drawer: Renderable = Renderable { example() }) : Screen(Component.empty()) {
    override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(
            gui
            //? if >=1.21 {
            /*, mouseX, mouseY, partialTick
            *///?}
        )
        ImGuiHandler.drawFrame(drawer)
    }
}

fun Graphics.example() {
    item(ItemStack(Items.DIAMOND_SWORD), 100f, 100f)
}