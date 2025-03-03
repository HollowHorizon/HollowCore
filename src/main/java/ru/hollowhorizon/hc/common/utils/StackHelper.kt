@file:JvmName("StackHelper")
package ru.hollowhorizon.hc.common.utils

import net.minecraft.world.item.ItemStack

fun ItemStack.areItemsEqual(with: ItemStack): Boolean {
    if (this.isEmpty && with.isEmpty) return true
    return !this.isEmpty && ItemStack.isSameItem(this, with)
}

fun ItemStack.areStacksEqual(with: ItemStack): Boolean {
    return this.areItemsEqual(with) && ItemStack.isSameItemSameTags(this, with)
}

fun ItemStack.canCombineStacks(stack2: ItemStack): Boolean {
    if (!this.isEmpty && stack2.isEmpty) return true
    return this.areStacksEqual(stack2) && (this.count + stack2.count) <= this.maxStackSize
}

fun ItemStack.canCombine(hand: ItemStack, count: Int, ingredientCount: Int): Boolean =
    this.canCombineStacks(hand) && count >= ingredientCount

fun ItemStack.withSize(size: Int, container: Boolean): ItemStack {
    var itemStack = this
    if (size <= 0) {
        return if (container && this.hasCraftingRemainder) {
            this.craftingRemainder
        } else {
            ItemStack.EMPTY
        }
    }

    itemStack = itemStack.copy()
    itemStack.count = size

    return itemStack
}

fun ItemStack.shrink(amount: Int, container: Boolean): ItemStack {
    if (this.isEmpty) return ItemStack.EMPTY

    return withSize(this.count - amount, container)
}

val ItemStack.hasCraftingRemainder get() = //? if fabric {
    this.recipeRemainder != ItemStack.EMPTY
 //?} elif forge {
    /*this.hasCraftingRemainingItem()
*///?}

val ItemStack.craftingRemainder get() = //? if fabric {
    this.recipeRemainder
//?} elif forge {
    /*this.craftingRemainingItem
*///?}