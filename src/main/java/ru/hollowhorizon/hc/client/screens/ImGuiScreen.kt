package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.client.imgui.Renderable

//? if >=1.20.1 {
/*import net.minecraft.client.gui.GuiGraphics
*///?}

open class ImGuiScreen(private val drawer: Renderable = Renderable { example() }) : Screen(Component.empty()) {
    //? if >=1.20.1 {
    /*override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    *///?} else {
    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
    //?}
        renderBackground(
            //? if >=1.21 {
            /*gui, mouseX, mouseY, partialTick
            *///?} elif >=1.20.1 {
            /*gui
            *///?} else {
            poseStack
            //?}
        )

        ImGuiHandler.drawFrame(drawer)
    }
}

fun Graphics.example() {
    item(ItemStack(Items.DIAMOND_SWORD), 100f, 100f)
}