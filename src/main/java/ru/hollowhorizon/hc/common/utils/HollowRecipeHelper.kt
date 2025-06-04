/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.utils

import com.google.gson.*
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
//? if !fabric
/*import net.minecraftforge.common.crafting.CraftingHelper*/
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.DefaultHollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import java.util.concurrent.ConcurrentHashMap

/**
 * Helper class for handling HollowCore recipe conditions and serialization.
 *
 * This class provides methods for registering condition and ingredient serializers,
 * processing conditions in JSON format, and handling item stack deserialization.
 */
object HollowRecipeHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val conditions = ConcurrentHashMap<ResourceLocation, HollowConditionSerializer<*>>()

    /**
     * Registers a new Hollow condition serializer.
     *
     * @param condition The serializer to register.
     * @throws IllegalStateException if a serializer with the same ID is already registered.
     */
    @JvmStatic
    fun register(condition: HollowConditionSerializer<*>) {
        val id = condition.id
        check(conditions.putIfAbsent(id, condition) == null) {
            "Hollow Condition Serializer $id already registered"
        }
    }

    /**
     * Registers a new Hollow ingredient serializer.
     *
     * @param serializer The serializer to register.
     */
    @JvmStatic
    fun register(serializer: HollowIngredientSerializer<*>) =
        DefaultHollowIngredient.registerSerializer(serializer)

    /**
     * Retrieves a registered ingredient serializer by its ID.
     *
     * @param id The resource location identifier of the serializer.
     * @return The serializer instance, or `null` if not found.
     */
    @JvmStatic
    fun getSerializer(id: ResourceLocation) = DefaultHollowIngredient.getSerializer(id)

    /**
     * Processes conditions in a JSON object.
     *
     * @param json The JSON object containing conditions.
     * @param memberName The key containing the condition array.
     * @param context The condition context.
     * @return `true` if conditions are met, `false` otherwise.
     */
    @JvmStatic
    fun processConditions(json: JsonObject, memberName: String, context: HollowCondition.ConditionContext) =
        !json.has(memberName) || processConditions(GsonHelper.getAsJsonArray(json, memberName), context)

    /**
     * Processes a list of conditions.
     *
     * @param conditions The JSON array of conditions.
     * @param context The condition context.
     * @return `true` if all conditions pass, `false` otherwise.
     * @throws JsonSyntaxException if the array contains invalid elements.
     */
    @JvmStatic
    fun processConditions(conditions: JsonArray, context: HollowCondition.ConditionContext): Boolean {
        for (i in 0 ..< conditions.size()) {
            val json = conditions.get(i)
            if (!json.isJsonObject)
                throw JsonSyntaxException("Conditions must be an array of JsonObjects")

            if (!getCondition(json.asJsonObject).test(context))
                return false
        }
        return true
    }

    /**
     * Retrieves a condition from a JSON object.
     *
     * @param jsonObject The JSON object containing condition data.
     * @return The parsed condition.
     * @throws JsonSyntaxException if the condition type is unknown.
     */
    @JvmStatic
    fun getCondition(jsonObject: JsonObject): HollowCondition {
        val type = ResourceLocation(GsonHelper.getAsString(jsonObject, "hollowcore:type"))
        val serializer = conditions[type] ?: throw JsonSyntaxException("Unknown condition type: $type")
        return serializer.fromJson(jsonObject)
    }

    /**
     * Serializes a condition into a JSON object.
     *
     * @param condition The condition to serialize.
     * @return The serialized JSON object.
     * @throws JsonSyntaxException if the condition type is unknown.
     */
    @JvmStatic
    fun <T: HollowCondition> serialize(condition: T): JsonObject {
        val serializer: HollowConditionSerializer<T>? = JavaHacks.forceCast(conditions[condition.id])
        serializer ?: throw JsonSyntaxException("Unknown condition type: ${condition.id}")
        return serializer.getJson(condition)
    }

    /**
     * Serializes multiple conditions into a JSON array.
     *
     * @param conditions The conditions to serialize.
     * @return The serialized JSON array.
     */
    @JvmStatic
    fun serialize(vararg conditions: HollowCondition): JsonArray {
        val arr = JsonArray()
        conditions.map { serialize(it) }.forEach(arr::add)
        return arr
    }

    /**
     * Retrieves an item stack from a JSON object.
     *
     * @param json The JSON object containing item stack data.
     * @param readNBT Whether to read NBT data (default: true).
     * @param disallowAir Whether to disallow air items (default: false).
     * @return The deserialized item stack.
     * @throws JsonSyntaxException if the item is unknown or invalid.
     */
    @JvmStatic
    fun getItemStack(json: JsonObject, readNBT: Boolean = true, disallowAir: Boolean = false): ItemStack {
        //? if fabric {
        val itemName = GsonHelper.getAsString(json, "item")
        val item = getItem(itemName, disallowAir)
        if (readNBT && json.has("nbt")) {
            val nbt = getNBT(json["nbt"])
            val tmp = CompoundTag()

            tmp.put("tag", nbt)
            tmp.putString("id", itemName)
            tmp.putInt("Count", GsonHelper.getAsInt(json, "count", 1))

            return ItemStack.of(tmp)
        }
        return ItemStack(item, GsonHelper.getAsInt(json, "count", 1))
        //?} else {
        /*return CraftingHelper.getItemStack(json, readNBT, disallowAir)
        *///?}
    }

    /**
     * Retrieves an item from its name.
     *
     * @param itemName The item name.
     * @param disallowsAirInRecipe Whether air is disallowed in recipes.
     * @return The item instance.
     * @throws JsonSyntaxException if the item is unknown or invalid.
     */
    @JvmStatic
    fun getItem(itemName: String, disallowsAirInRecipe: Boolean): Item {
        val itemKey = ResourceLocation(itemName)
        val item = BuiltInRegistries.ITEM.getOptional(itemKey).orElseThrow { JsonSyntaxException("Unknown item '$itemName'") }
        if (disallowsAirInRecipe && item === Items.AIR) throw JsonSyntaxException("Invalid item: $itemName")
        return item
    }

    /**
     * Parses NBT data from a JSON element.
     *
     * @param element The JSON element containing NBT data.
     * @return The parsed compound tag.
     * @throws JsonSyntaxException if the NBT entry is invalid.
     */
    @JvmStatic
    fun getNBT(element: JsonElement): CompoundTag {
        try {
            return if (element.isJsonObject) TagParser.parseTag(gson.toJson(element))
            else TagParser.parseTag(GsonHelper.convertToString(element, "nbt"))
        } catch (e: CommandSyntaxException) {
            throw JsonSyntaxException("Invalid NBT Entry: $e")
        }
    }
}
