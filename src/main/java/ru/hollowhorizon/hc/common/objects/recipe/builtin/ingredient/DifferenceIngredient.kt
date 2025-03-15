package ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.objects.recipe.deep.requireTesting
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import ru.hollowhorizon.hc.common.utils.rl

/**
 * Represents an ingredient that matches if it is present in the base ingredient but not in the subtracted ingredient.
 *
 * You can read more [here](https://0mods.team/docs/hollowcore/ingredient/#differenceingredient)
 *
 * Example usage in JSON:
 * ```json
 * {
 *   "ingredients": [
 *     {
 *       "hollowcore:type": "hollowcore:difference",
 *       "base": { "tag": "minecraft:items/planks" },
 *       "subtracted": {
 *         "hollowcore:type": "any",
 *         "ingredients": [
 *           { "item": "minecraft:oak_planks" },
 *           { "item": "minecraft:acacia_planks" }
 *         ]
 *       }
 *     }
 *   ]
 * }
 * ```
 *
 * @param base The base ingredient set.
 * @param subtracted The ingredient set to be excluded from the base.
 */
class DifferenceIngredient(private val base: Ingredient, private val subtracted: Ingredient): HollowIngredient {
    override fun test(stack: ItemStack): Boolean = base.test(stack) && !subtracted.test(stack)

    override fun requireTesting(): Boolean = base.requireTesting || subtracted.requireTesting

    override val items: List<ItemStack>
        get() {
            val stacks = ArrayList(base.items.asList())
            stacks.removeIf(subtracted)
            return stacks
        }

    override val serializer: HollowIngredientSerializer<*> = Serializer

    private class Serializer: HollowIngredientSerializer<DifferenceIngredient> {
        override val id: ResourceLocation
            get() = "${HollowCore.MODID}:difference".rl

        override fun fromJson(json: JsonObject): DifferenceIngredient {
            val base = Ingredient.fromJson(json.get("base"))
            val subtracted = Ingredient.fromJson(json.get("subtracted"))
            return DifferenceIngredient(base, subtracted)
        }

        override fun fromNetwork(buf: FriendlyByteBuf): DifferenceIngredient {
            val base = Ingredient.fromNetwork(buf)
            val subtracted = Ingredient.fromNetwork(buf)
            return DifferenceIngredient(base, subtracted)
        }

        override fun toNetwork(buf: FriendlyByteBuf, ingredient: DifferenceIngredient) {
            ingredient.base.toNetwork(buf)
            ingredient.subtracted.toNetwork(buf)
        }

        override fun toJson(json: JsonObject, ingredient: DifferenceIngredient) {
            json.add("base", ingredient.base.toJson())
            json.add("subtracted", ingredient.subtracted.toJson())
        }
    }

    companion object {
        @JvmField val Serializer: HollowIngredientSerializer<DifferenceIngredient> = Serializer()
    }
}