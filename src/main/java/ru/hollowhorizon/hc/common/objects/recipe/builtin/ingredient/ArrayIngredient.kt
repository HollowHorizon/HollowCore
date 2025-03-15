package ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.objects.recipe.deep.requireTesting
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import ru.hollowhorizon.hc.common.utils.rl
import java.util.*

/**
 * Represents a composite ingredient consisting of multiple sub-ingredients.
 * This abstract class allows the creation of ingredients that require multiple
 * individual ingredients to match an [ItemStack].
 *
 * Subclasses define how multiple ingredients are evaluated together, such as
 * requiring any or all of them to match.
 *
 * @property ingredients An array of [Ingredient] objects representing the sub-ingredients.
 */
abstract class ArrayIngredient protected constructor(protected val ingredients: Array<Ingredient>): HollowIngredient {
    init {
        require(ingredients.isNotEmpty()) { "Array ingredient must have at least one sub-ingredient" }
    }

    override fun requireTesting(): Boolean {
        this.ingredients.forEach {
            if (it.requireTesting) return true
        }

        return false
    }

    /**
     * Represents an ingredient that matches if any of its sub-ingredients match.
     *
     * You can read more [here](https://0mods.team/docs/hollowcore/ingredient/#anyingredient)
     *
     * Example usage in JSON:
     * ```json
     * {
     *   "ingredients": [
     *     {
     *       "hollowcore:type": "hollowcore:any",
     *       "ingredients": [
     *         { "item": "minecraft:apple" },
     *         { "item": "minecraft:gold_apple" },
     *         { "item": "minecraft:diamond" }
     *       ]
     *     }
     *   ]
     * }
     * ```
     *
     * @param ingredients An array of [Ingredient] objects, at least one of which must match.
     */
    class AnyIngredient(ingredients: Array<Ingredient>) : ArrayIngredient(ingredients) {
        override fun test(stack: ItemStack): Boolean {
            this.ingredients.forEach {
                if (it.test(stack)) return true
            }

            return false
        }

        override val items: List<ItemStack>
            get() {
                val arrl = arrayListOf<ItemStack>()
                this.ingredients.forEach { arrl.addAll(it.items.asList()) }

                return arrl
            }
        override val serializer: HollowIngredientSerializer<*> = Serializer

        companion object {
            @JvmField
            val Serializer: HollowIngredientSerializer<AnyIngredient> = Serializer("${HollowCore.MODID}:any".rl, ArrayIngredient::AnyIngredient)
        }
    }

    /**
     * Represents an ingredient that matches only if all of its sub-ingredients match.
     *
     * You can read more [here](https://0mods.team/docs/hollowcore/ingredient/#allingredient)
     *
     * Example usage in JSON:
     * ```json
     * {
     *   "ingredients": [
     *     {
     *       "hollowcore:type": "hollowcore:all",
     *       "ingredients": [
     *         { "item": "minecraft:apple" },
     *         { "item": "minecraft:gold_apple" },
     *         { "item": "minecraft:diamond" }
     *       ]
     *     }
     *   ]
     * }
     * ```
     *
     * @param ingredients An array of [Ingredient] objects, all of which must match.
     */
    class AllIngredient(ingredients: Array<Ingredient>): ArrayIngredient(ingredients) {
        override fun test(stack: ItemStack): Boolean {
            this.ingredients.forEach { if (!it.test(stack)) return false }
            return true
        }

        override val items: List<ItemStack> get() {
            val previewStacks = ArrayList(ingredients[0].items.asList())

            for (i in 1..< ingredients.size) {
                val ing = ingredients[i]
                previewStacks.removeIf { stack: ItemStack? -> !ing.test(stack) }
            }

            return previewStacks
        }

        override val serializer: HollowIngredientSerializer<*> = Serializer

        companion object {
            @JvmField
            val Serializer: HollowIngredientSerializer<AllIngredient> = Serializer("${HollowCore.MODID}:all".rl, ArrayIngredient::AllIngredient)
        }
    }

    private class Serializer<T: ArrayIngredient>(override val id: ResourceLocation, private val factory: (Array<Ingredient>) -> T): HollowIngredientSerializer<T> {
        override fun fromJson(json: JsonObject): T {
            val values = GsonHelper.getAsJsonArray(json, "ingredients")
            // non null ingredient
            val ingredients = Array(values.size()) { Ingredient.EMPTY }

            for (i in 0..<values.size()) {
                ingredients[i] = Ingredient.fromJson(values[i])
            }
            return factory(ingredients)
        }

        override fun toJson(json: JsonObject, ingredient: T) {
            val values = JsonArray()

            for (value in ingredient.ingredients) {
                values.add(value.toJson())
            }

            json.add("ingredients", values)
        }

        override fun fromNetwork(buf: FriendlyByteBuf): T {
            val size = buf.readVarInt()
            val ingredients = Array(size) { Ingredient.EMPTY }

            for (i in 0..< size) {
                ingredients[i] = Ingredient.fromNetwork(buf)
            }

            return factory(ingredients)
        }

        override fun toNetwork(buf: FriendlyByteBuf, ingredient: T) {
            buf.writeVarInt(ingredient.ingredients.size)

            for (value in ingredient.ingredients) {
                value.toNetwork(buf)
            }
        }
    }
}