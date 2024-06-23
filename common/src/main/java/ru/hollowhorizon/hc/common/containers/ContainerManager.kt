package ru.hollowhorizon.hc.common.containers

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.addons.ImGuiInventory.ITEM_SIZES

interface ContainerManager {
}

object ClientContainerManager : ContainerManager {
    var holdStack = ItemStack.EMPTY

    fun clickSlot(container: Container, id: Int, leftButton: Boolean): Boolean {
        val item = container.getItem(id)
        val slots = ITEM_SIZES.filter { it.value.isPlaced }.map { it.key }

        if (holdStack.isEmpty && !item.isEmpty) {
            if (leftButton) {
                if (Screen.hasShiftDown()) {
                    if (container is Inventory) {
                        val original = container.getItem(id)
                        val range = if (id in (0..<9)) (9..<36) else (0..<9)

                        val slots = range.map { it to container.getItem(it) }
                            .filter { ItemStack.isSameItemSameComponents(original, it.second) or it.second.isEmpty }
                            .sortedBy { it.first }

                        var i = 0
                        while (i < slots.size && original.count > 0) {
                            val slotStack = container.getItem(slots[i].first)

                            if(slotStack.isEmpty) {
                                container.setItem(slots[i].first, original)
                                container.setItem(id, ItemStack.EMPTY)
                                return false
                            }

                            val remaining = slotStack.maxStackSize - slotStack.count
                            slotStack.count = (slotStack.count + original.count).coerceAtMost(slotStack.maxStackSize)
                            if(remaining > 0) {
                                original.shrink(remaining)
                                container.setItem(slots[i].first, slotStack)
                            }

                            i++
                        }
                        container.setItem(id, original)
                    }
                } else {
                    holdStack = item.copy()
                    //Взять целиком
                    container.setItem(id, ItemStack.EMPTY)
                    return true
                }
            } else {
                holdStack = item.copy()
                // взять половину
                if (holdStack.count > 1) {
                    val newItem = item.copy().apply { count /= 2 }
                    container.setItem(id, newItem)
                    holdStack.count -= newItem.count
                } else {
                    container.setItem(id, ItemStack.EMPTY)
                }
                return true
            }
        } else {
            if (leftButton) {
                if (item.isEmpty) {
                    if (!holdStack.isEmpty) {
                        //Положить всё
                        if (ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                            val slots = (0..<container.containerSize).map { it to container.getItem(it) }
                                .filter { ItemStack.isSameItemSameComponents(holdStack, it.second) }
                                .sortedBy { it.second.count }

                            val result = holdStack.copy()
                            var i = 0
                            while (i < slots.size && result.count < result.maxStackSize) {
                                val slotStack = container.getItem(slots[i].first)

                                val remaining = result.maxStackSize - result.count
                                result.count = (result.count + slotStack.count).coerceAtMost(result.maxStackSize)
                                if (remaining > 0) {
                                    slotStack.shrink(remaining)
                                    container.setItem(slots[i].first, slotStack)
                                }

                                i++
                            }
                            holdStack = result
                        } else {
                            container.setItem(id, holdStack.copy())
                            holdStack = ItemStack.EMPTY
                        }
                        return true
                    }
                } else {
                    if (ItemStack.isSameItemSameComponents(holdStack, item)) {
                        if (item.count != item.maxStackSize) {
                            val count = holdStack.count
                            val remain = item.maxStackSize - item.count
                            container.setItem(
                                id,
                                item.copy().apply { this.count = (this.count + count).coerceAtMost(maxStackSize) })
                            holdStack.shrink(remain)
                            return true
                        }
                    } else {
                        container.setItem(id, holdStack.copy())
                        holdStack = item
                        return true
                    }
                }
            } else {
                if (item.isEmpty) {
                    container.setItem(id, holdStack.copy().apply { count = 1 })
                    holdStack.shrink(1)
                    return true
                } else if (ItemStack.isSameItemSameComponents(holdStack, item) && item.count != item.maxStackSize) {
                    container.setItem(id, item.copy().apply { count++ })
                    holdStack.shrink(1)
                    return true
                }
            }
        }


        return slots.isNotEmpty()
    }
}

fun main() {
    println(3 / 2)
}

object ServerContainerManager : ContainerManager {

}