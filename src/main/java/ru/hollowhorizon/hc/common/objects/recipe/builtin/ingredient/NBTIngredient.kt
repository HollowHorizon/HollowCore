package ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.mojang.serialization.JsonOps
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.TagParser
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.utils.rl
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import java.util.Objects

/**
 * Represents an ingredient that checks for a specific NBT tag in addition to its base ingredient.
 *
 * You can read more [here](https://0mods.team/docs/hollowcore/ingredient/#nbtingredient)
 *
 * Example usage in JSON:
 * ```json
 * {
 *   "ingredients": [
 *     {
 *       "hollowcore:type": "hollowcore:nbt",
 *       "base": { "item": "minecraft:stick" },
 *       "nbt": { "Enchantments": [{ "id": "efficiency", "lvl": 3 }] },
 *       "strict": false
 *     }
 *   ]
 * }
 * ```
 *
 * @param original The base ingredient to check.
 * @param tag The NBT data to match against.
 * @param strict If true, the item's NBT must match exactly; if false, the item must have the specified NBT.
 */
class NBTIngredient(
    private val original: Ingredient,
    private val tag: CompoundTag?,
    private val strict: Boolean
): HollowIngredient {
    init {
        require(!(tag == null && !strict)) { "NBTIngredient can only have null NBT in strict mode" }
    }

    override fun test(stack: ItemStack): Boolean {
        if (!original.test(stack)) return false

        return if (strict) Objects.equals(tag, stack.tag) else NbtUtils.compareNbt(tag, stack.tag, true)
    }

    override fun requireTesting(): Boolean = true

    override val items: List<ItemStack>
        get() {
            val stacks = arrayListOf(*original.items)
            stacks.replaceAll {
                val c = it.copy()
                if (tag != null) {
                    c.tag = tag.copy()
                }
                c
            }

            stacks.removeIf { !original.test(it) }
            return stacks
        }

    override val serializer: HollowIngredientSerializer<*> = Serializer

    companion object {
        @JvmField
        val Serializer: HollowIngredientSerializer<NBTIngredient> = Serializer()
    }

    private class Serializer : HollowIngredientSerializer<NBTIngredient> {
        override val id: ResourceLocation = "${HollowCore.MODID}:nbt".rl

        override fun fromJson(json: JsonObject): NBTIngredient {
            val item = Ingredient.fromJson(json.get("base"))
            val tag = this.decodeTag(json.get("nbt"))
            val strict = GsonHelper.getAsBoolean(json, "strict", true)
            return NBTIngredient(item, tag, strict)
        }

        override fun fromNetwork(buf: FriendlyByteBuf): NBTIngredient {
            val ingr = Ingredient.fromNetwork(buf)
            val tag = buf.readNbt()
            val strict = buf.readBoolean()

            return NBTIngredient(ingr, tag, strict)
        }

        override fun toNetwork(buf: FriendlyByteBuf, ingredient: NBTIngredient) {
            ingredient.original.toNetwork(buf)
            buf.writeNbt(ingredient.tag)
            buf.writeBoolean(ingredient.strict)
        }

        override fun toJson(json: JsonObject, ingredient: NBTIngredient) {
            json.add("base", ingredient.original.toJson())
            json.addProperty("strict", ingredient.strict)

            if (ingredient.tag != null)
                json.add("nbt", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, ingredient.tag))
        }

        private fun decodeTag(json: JsonElement?): CompoundTag? {
            if (json == null || json.isJsonNull) return null
            return try {
                if (json.isJsonObject) TagParser.parseTag(json.toString())
                else TagParser.parseTag(GsonHelper.convertToString(json, "nbt"))
            } catch (e: Exception) {
                throw JsonSyntaxException("Invalid nbt tag: ${e.message}")
            }
        }
    }
}