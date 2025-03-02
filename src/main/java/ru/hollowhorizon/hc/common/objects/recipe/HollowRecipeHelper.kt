package ru.hollowhorizon.hc.common.objects.recipe

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.DefaultHollowIngredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredientSerializer
import java.util.concurrent.ConcurrentHashMap

object HollowRecipeHelper {
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
        !json.has(memberName) || this.processConditions(GsonHelper.getAsJsonArray(json, memberName), context)

    @JvmStatic
    fun processConditions(conditions: JsonArray, context: HollowCondition.ConditionContext): Boolean {
        for (i in 0 ..< conditions.size()) {
            val json = conditions.get(i)
            if (!json.isJsonObject)
                throw JsonSyntaxException("Conditions must be an array of JsonObjects")

            if (!this.getCondition(json.asJsonObject).test(context))
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
}
