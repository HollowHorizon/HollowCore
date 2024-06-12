package ru.hollowhorizon.hc.client.imgui.addons

import imgui.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods

fun ImGuiMethods.inventory(): ItemStack {
    val player = Minecraft.getInstance().player ?: return ItemStack.EMPTY
    var stack = ItemStack.EMPTY
    val npc: LivingEntity

    player.inventory.items.subList(9, 36).forEachIndexed { index, itemStack ->
        if (item(itemStack, 64f, 64f, border = true)) stack = itemStack
        if ((index + 1) % 9 != 0) ImGui.sameLine()
    }
    ImGui.setCursorPosY(ImGui.getCursorPosY()+5)
    player.inventory.items.subList(0, 9).forEachIndexed { index, itemStack ->
        if (item(itemStack, 64f, 64f, border = true)) stack = itemStack
        if ((index + 1) % 9 != 0) ImGui.sameLine()
    }
    return stack
}