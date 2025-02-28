package ru.hollowhorizon.hc.common.objects.recipe

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

interface IHollowIngredient {
    fun test(stack: ItemStack): Boolean

    fun requireTesting(): Boolean

    val items: List<ItemStack>

    val serializer: IHollowIngredientSerializer<*>

    val asVanilla: Ingredient get() = HollowIngredient(this)
}
