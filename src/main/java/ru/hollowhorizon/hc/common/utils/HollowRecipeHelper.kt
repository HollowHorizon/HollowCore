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
import net.minecraft.world.item.crafting.ShapedRecipe
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.DefaultHollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrElse

object HollowRecipeHelper {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val conditions = ConcurrentHashMap<ResourceLocation, HollowConditionSerializer<*>>()

    @JvmStatic
    fun register(condition: HollowConditionSerializer<*>) {
        val id = condition.id
        check(conditions.putIfAbsent(id, condition) == null) {
            "Hollow Condition Serializer $id already registered"
        }
    }

    @JvmStatic
    fun register(serializer: HollowIngredientSerializer<*>) =
        DefaultHollowIngredient.registerSerializer(serializer)

    @JvmStatic
    fun getSerializer(id: ResourceLocation) = DefaultHollowIngredient.getSerializer(id)

    @JvmStatic
    fun processConditions(json: JsonObject, memberName: String, context: HollowCondition.ConditionContext) =
        !json.has(memberName) || processConditions(GsonHelper.getAsJsonArray(json, memberName), context)

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

    @JvmStatic
    fun getCondition(jsonObject: JsonObject): HollowCondition {
        val type = ResourceLocation(GsonHelper.getAsString(jsonObject, "hollowcore:type"))
        val serializer = conditions[type] ?: throw JsonSyntaxException("Unknown condition type: $type")
        return serializer.fromJson(jsonObject)
    }

    @JvmStatic
    fun <T: HollowCondition> serialize(condition: T): JsonObject {
        val serializer: HollowConditionSerializer<T>? = JavaHacks.forceCast(conditions[condition.id])
        serializer ?: throw JsonSyntaxException("Unknown condition type: ${condition.id}")
        return serializer.getJson(condition)
    }

    @JvmStatic
    fun serialize(vararg conditions: HollowCondition): JsonArray {
        val arr = JsonArray()
        conditions.map { serialize(it) }.forEach(arr::add)
        return arr
    }

    @JvmStatic
    fun getItemStack(json: JsonObject, readNBT: Boolean = true, disallowAir: Boolean = false): ItemStack {
        val itemName = GsonHelper.getAsString(json, "item")
        val item = getItem(itemName, disallowAir)
        if (readNBT && json.has("nbt")) {
            val nbt = getNBT(json["nbt"])
            val tmp = CompoundTag()
            //? if !fabric {
            /*if (nbt.contains("ForgeCaps")) {
                nbt["ForgeCaps"]?.let { tmp.put("ForgeCaps", it) }
                nbt.remove("ForgeCaps")
            }
            *///?}

            tmp.put("tag", nbt)
            tmp.putString("id", itemName)
            tmp.putInt("Count", GsonHelper.getAsInt(json, "count", 1))

            return ItemStack.of(tmp)
        }

        return ItemStack(item, GsonHelper.getAsInt(json, "count", 1))
    }

    @JvmStatic
    fun getItem(itemName: String, disallowsAirInRecipe: Boolean): Item {
        val itemKey = ResourceLocation(itemName)
        val item = BuiltInRegistries.ITEM.getOptional(itemKey).orElseThrow { JsonSyntaxException("Unknown item '$itemName'") }
        if (disallowsAirInRecipe && item === Items.AIR) throw JsonSyntaxException("Invalid item: $itemName")
        return item
    }

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
