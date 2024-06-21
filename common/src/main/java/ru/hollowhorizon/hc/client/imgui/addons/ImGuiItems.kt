package ru.hollowhorizon.hc.client.imgui.addons

import imgui.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.ImGuiInventory.slot
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods

open class ItemProperties {
    open var red = 1f
    open var green = 1f
    open var blue = 1f
    open var alpha = 1f
    open var disableResize = false
    open var rotation = 0f
    open var tooltip = true
    open var scale = 1f
    open var alwaysOnTop = false

    open fun update(hovered: Boolean) {

    }
}

fun ImGuiMethods.inventory(): ItemStack {
    val player = Minecraft.getInstance().player ?: return ItemStack.EMPTY
    val stack = ItemStack.EMPTY

    player.inventory.items.subList(9, 36).forEachIndexed { index, itemStack ->
        slot(9 + index, itemStack, 64f) {
            if (itemStack.isEmpty || it.isEmpty) player.inventory.setItem(9 + index, it)
            else player.inventory.add(9 + index, it)
        }
        if ((index + 1) % 9 != 0) ImGui.sameLine()
    }
    ImGui.setCursorPosY(ImGui.getCursorPosY() + 5)
    player.inventory.items.subList(0, 9).forEachIndexed { index, itemStack ->
        slot(index, itemStack, 64f) {
            if (itemStack.isEmpty || it.isEmpty) player.inventory.setItem(index, it)
            else player.inventory.add(index, it)
        }
        if ((index + 1) % 9 != 0) ImGui.sameLine()
    }
    return stack
}