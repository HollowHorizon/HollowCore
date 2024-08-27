package ru.hollowhorizon.hc.client.imgui.addons

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import net.minecraft.client.Minecraft
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.imgui.addons.ImGuiInventory.slot
import ru.hollowhorizon.hc.common.containers.ClientContainerManager

open class ContainerProvider(val container: Container) {
    open fun draw() {
        for (i in 0..<container.containerSize) {
            val item = container.getItem(i)

            slot(i, item)
            if ((i + 1) % 9 != 0) ImGui.sameLine()
        }

        splitItems()
        update()
    }

    fun update() {
        previousContainer = container
    }

    fun slot(i: Int, item: ItemStack) {
        Graphics.slot(i, item, slotSize, container = container)
    }

    fun splitItems() {
        return // TODO: Make it work on server

        val slots = ImGuiInventory.ITEM_SIZES[container]!!.filter { it.value.isPlaced }.map { it.key }

        if (slots.size > 1 && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            val item = slots.map { container.getItem(it) }.firstOrNull { !it.isEmpty }
            if (item != null) {
                val count = slots.sumOf { container.getItem(it).count } + (ClientContainerManager.PLAYERS_HOLD_STACKS[Minecraft.getInstance().player!!.uuid]?.count ?: 0)
                val eachCount = count / slots.size
                val holdCount = count % slots.size

                slots.forEach {
                    container.setItem(
                        it,
                        item.copy().apply { this.count = eachCount })
                }
                item.count = holdCount
                ClientContainerManager.PLAYERS_HOLD_STACKS[Minecraft.getInstance().player!!.uuid] = item
            }
        }
    }

    open val slotSize = 80f

    companion object {
        var previousContainer: Container? = null
    }
}

class InventoryContainer(container: Container) : ContainerProvider(container) {
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

        splitItems()
        update()
    }
}

val Container.defaultProvider get() = ContainerProvider(this)
val Container.inventoryProvider get() = InventoryContainer(this)