package ru.hollowhorizon.hc.common.objects.recipe.ingredient

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

/**
 * Represents a hollow ingredient that can be tested against an [ItemStack].
 * This interface provides methods to check if an item matches the ingredient criteria,
 * determine whether testing is required, and retrieve serialized ingredient data.
 */
interface HollowIngredient {
    /**
     * Tests whether the given [ItemStack] matches the ingredient criteria.
     *
     * @param stack the [ItemStack] to test
     * @return `true` if the stack matches the ingredient, otherwise `false`
     */
    fun test(stack: ItemStack): Boolean

    /**
     * Checks if the ingredient requires explicit testing.
     *
     * @return `true` if testing is required, otherwise `false`
     */
    fun requireTesting(): Boolean

    /**
     * Retrieves the list of item stacks that represent this ingredient.
     *
     * @return a list of [ItemStack] objects
     */
    val items: List<ItemStack>

    /**
     * Gets the serializer responsible for serializing and deserializing this ingredient.
     *
     * @return the [HollowIngredientSerializer] instance
     */
    val serializer: HollowIngredientSerializer<*>

    /**
     * Converts this hollow ingredient into a vanilla [Ingredient].
     *
     * @return a vanilla-compatible [Ingredient] representation of this ingredient
     */
    val asVanilla: Ingredient get() = DefaultHollowIngredient(this)
}
