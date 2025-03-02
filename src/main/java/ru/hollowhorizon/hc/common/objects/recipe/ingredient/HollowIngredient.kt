package ru.hollowhorizon.hc.common.objects.recipe.ingredient

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

interface HollowIngredient {
    fun test(stack: ItemStack): Boolean

    fun requireTesting(): Boolean

    val items: List<ItemStack>

    val serializer: HollowIngredientSerializer<*>

    val asVanilla: Ingredient get() = DefaultHollowIngredient(this)
}
