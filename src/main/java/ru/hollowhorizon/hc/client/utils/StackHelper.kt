package ru.hollowhorizon.hc.client.utils

import net.minecraft.world.item.ItemStack

object StackHelper {
    @JvmStatic
    fun ItemStack.areItemsEqual(with: ItemStack): Boolean {
        if (this.isEmpty && with.isEmpty) return true
        return !this.isEmpty && ItemStack.isSameItem(this, with)
    }

    @JvmStatic
    fun ItemStack.areStacksEqual(with: ItemStack): Boolean {
        return this.areItemsEqual(with) && ItemStack.isSameItemSameTags(this, with)
    }

    @JvmStatic
    fun ItemStack.canCombineStacks(stack2: ItemStack): Boolean {
        if (!this.isEmpty && stack2.isEmpty) return true
        return this.areStacksEqual(stack2) && (this.count + stack2.count) <= this.maxStackSize
    }

    @JvmStatic
    fun ItemStack.canCombine(hand: ItemStack, count: Int, ingredientCount: Int): Boolean =
        this.canCombineStacks(hand) && count >= ingredientCount
}