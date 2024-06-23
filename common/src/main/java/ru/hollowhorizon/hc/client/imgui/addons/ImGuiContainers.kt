package ru.hollowhorizon.hc.client.imgui.addons

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiWindowFlags
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods
import ru.hollowhorizon.hc.client.imgui.addons.ImGuiInventory.slot
import ru.hollowhorizon.hc.common.containers.ClientContainerManager

open class ContainerProvider(val container: Container) {
    open fun draw() {
        for (i in 0..<container.containerSize) {
            val item = container.getItem(i)

            slot(i, item)
            if ((i + 1) % 9 != 0) ImGui.sameLine()
        }
    }

    fun slot(i: Int, item: ItemStack) {
        ImGuiMethods.slot(i, item, slotSize, container = container)
    }

    open val slotSize = 80f
}

val Container.defaultProvider get() = ContainerProvider(this)
val Container.inventoryProvider
    get() = object : ContainerProvider(this) {
        override fun draw() {
            for (i in 9..<36) {
                val item = container.getItem(i)

                slot(i, item)
                if ((i + 1) % 9 != 0) ImGui.sameLine()
            }

            ImGui.setCursorPosY(ImGui.getCursorPosY() + 5)

            for (i in 0..<9) {
                val item = container.getItem(i)

                slot(i, item)
                if ((i + 1) % 9 != 0) ImGui.sameLine()
            }

            val slots = ImGuiInventory.ITEM_SIZES.filter { it.value.isPlaced }.map { it.key }


            if (slots.size > 1 && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                val item = slots.map { container.getItem(it) }.firstOrNull { !it.isEmpty }
                if (item != null) {
                    val count = slots.sumOf { container.getItem(it).count } + ClientContainerManager.holdStack.count
                    val eachCount = count / slots.size
                    val holdCount = count % slots.size

                    slots.forEach {
                        container.setItem(
                            it,
                            item.copy().apply { this.count = eachCount })
                    }
                    item.count = holdCount
                    ClientContainerManager.holdStack = item
                }
            }
        }
    }