package ru.hollowhorizon.hc.common.containers

import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import net.minecraft.world.Container
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.addons.ImGuiInventory.ITEM_SIZES
import ru.hollowhorizon.hc.common.capabilities.containers.HollowContainer
import ru.hollowhorizon.hc.common.events.container.ContainerEvent
import ru.hollowhorizon.hc.common.events.post

interface ContainerManager {
    var holdStack: ItemStack
    val isClient: Boolean

    fun clickSlot(
        fromContainer: Container,
        toContainer: Container,
        id: Int,
        leftButton: Boolean,
        hasShift: Boolean,
    ): Boolean {
        val item = fromContainer.getItem(id)

        if (holdStack.isEmpty && !item.isEmpty) {
            if (!fromContainer.canTakeItem(toContainer, id, item)) return false

            if (fromContainer is HollowContainer) {
                val event = ContainerEvent.OnTake(fromContainer, id)
                event.post()
                if (event.isCanceled) return false
            }

            if (leftButton) {
                if (hasShift) {
                    if (fromContainer is Inventory && fromContainer == toContainer) {
                        val original = fromContainer.getItem(id)
                        val range = if (id in (0..<9)) (9..<36) else (0..<9)

                        val slots = range
                            .filter { fromContainer.canPlaceItem(it, original) }
                            .map { it to fromContainer.getItem(it) }
                            .filter { ItemStack.isSameItemSameComponents(original, it.second) or it.second.isEmpty }
                            .sortedBy { it.first }

                        var i = 0
                        while (i < slots.size && original.count > 0) {
                            val slotStack = fromContainer.getItem(slots[i].first)

                            if (slotStack.isEmpty) {
                                fromContainer.setItem(slots[i].first, original)
                                fromContainer.setItem(id, ItemStack.EMPTY)
                                return true
                            }

                            val remaining = slotStack.maxStackSize - slotStack.count
                            slotStack.count = (slotStack.count + original.count).coerceAtMost(slotStack.maxStackSize)
                            if (remaining > 0) {
                                original.shrink(remaining)
                                fromContainer.setItem(slots[i].first, slotStack)
                            }

                            i++
                        }
                        fromContainer.setItem(id, original)
                    } else {
                        val original = fromContainer.getItem(id)
                        val range = (0..<toContainer.containerSize)

                        val slots = range
                            .filter { toContainer.canPlaceItem(it, original) }
                            .map { it to toContainer.getItem(it) }
                            .filter { ItemStack.isSameItemSameComponents(original, it.second) or it.second.isEmpty }
                            .sortedBy { it.first }

                        var i = 0
                        while (i < slots.size && original.count > 0) {
                            val slotStack = toContainer.getItem(slots[i].first)

                            if (slotStack.isEmpty) {
                                toContainer.setItem(slots[i].first, original)
                                fromContainer.setItem(id, ItemStack.EMPTY)
                                return true
                            }

                            val remaining = slotStack.maxStackSize - slotStack.count
                            slotStack.count = (slotStack.count + original.count).coerceAtMost(slotStack.maxStackSize)
                            if (remaining > 0) {
                                original.shrink(remaining)
                                toContainer.setItem(slots[i].first, slotStack)
                            }

                            i++
                        }
                        fromContainer.setItem(id, original)
                    }
                } else {
                    holdStack = item.copy()
                    //Взять целиком
                    fromContainer.setItem(id, ItemStack.EMPTY)
                    return true
                }
            } else {
                holdStack = item.copy()
                // взять половину
                if (holdStack.count > 1) {
                    val newItem = item.copy().apply { count /= 2 }
                    fromContainer.setItem(id, newItem)
                    holdStack.count -= newItem.count
                } else {
                    fromContainer.setItem(id, ItemStack.EMPTY)
                }
                return true
            }
        } else {

            if (leftButton) {
                if (item.isEmpty) {
                    if (!holdStack.isEmpty) {
                        //Положить всё
                        if (ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                            val slots = (0..<fromContainer.containerSize)
                                .filter { toContainer.canPlaceItem(it, holdStack) }
                                .map { it to fromContainer.getItem(it) }
                                .filter { ItemStack.isSameItemSameComponents(holdStack, it.second) }
                                .sortedBy { it.second.count }

                            val result = holdStack.copy()
                            var i = 0
                            while (i < slots.size && result.count < result.maxStackSize) {
                                val slotStack = fromContainer.getItem(slots[i].first)

                                val remaining = result.maxStackSize - result.count
                                result.count = (result.count + slotStack.count).coerceAtMost(result.maxStackSize)
                                if (remaining > 0) {
                                    slotStack.shrink(remaining)
                                    fromContainer.setItem(slots[i].first, slotStack)
                                }

                                i++
                            }
                            holdStack = result
                        } else {
                            if (!fromContainer.canPlaceItem(id, holdStack.copy())) return false
                            fromContainer.setItem(id, holdStack.copy())
                            holdStack = ItemStack.EMPTY
                        }
                        return true
                    }
                } else {
                    if (ItemStack.isSameItemSameComponents(holdStack, item)) {
                        if (item.count != item.maxStackSize) {
                            val count = holdStack.count
                            val remain = item.maxStackSize - item.count
                            val itemToPlace =
                                item.copy().apply { this.count = (this.count + count).coerceAtMost(maxStackSize) }
                            if (!fromContainer.canPlaceItem(id, itemToPlace)) return false
                            fromContainer.setItem(id, itemToPlace)
                            holdStack.shrink(remain)
                            return true
                        }
                    } else {
                        if (!fromContainer.canPlaceItem(id, holdStack.copy())) return false
                        fromContainer.setItem(id, holdStack.copy())
                        holdStack = item
                        return true
                    }
                }
            } else {
                if (!fromContainer.canPlaceItem(id, holdStack)) return false

                if (item.isEmpty) {
                    fromContainer.setItem(id, holdStack.copy().apply { count = 1 })
                    holdStack.shrink(1)
                    return true
                } else if (ItemStack.isSameItemSameComponents(holdStack, item) && item.count != item.maxStackSize) {
                    fromContainer.setItem(id, item.copy().apply { count++ })
                    holdStack.shrink(1)
                    return true
                }
            }
        }


        return false
    }

}

object ClientContainerManager : ContainerManager {
    override var holdStack = ItemStack.EMPTY
    override val isClient = true

    override fun clickSlot(
        fromContainer: Container,
        toContainer: Container,
        id: Int,
        leftButton: Boolean,
        hasShift: Boolean,
    ): Boolean {
        val slots = ITEM_SIZES[fromContainer]!!.filter { it.value.isPlaced }.map { it.key }


        val capability =
            (fromContainer as? HollowContainer)?.capability ?: (toContainer as? HollowContainer)?.capability

        SyncEntityContainerPacket(
            (capability?.provider as? Entity)?.id ?: 0,
            capability?.javaClass?.name ?: "",
            capability?.containers?.indexOf(fromContainer) ?: -1,
            capability?.containers?.indexOf(toContainer) ?: -1,
            id,
            leftButton,
            hasShift
        ).send()
        return super.clickSlot(fromContainer, toContainer, id, leftButton, hasShift) || slots.isNotEmpty()
    }
}

object ServerContainerManager : ContainerManager {
    override var holdStack = ItemStack.EMPTY
    override val isClient = false
}