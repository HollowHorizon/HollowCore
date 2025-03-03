package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.common.base.Joiner
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.common.objects.recipe.HollowRecipeHelper
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.rl

class AndCondition(val values: Array<HollowCondition>): HollowCondition {
    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean {
        this.values.forEach { if (!it.test(context)) return false }

        return true
    }

    override fun toString(): String = Joiner.on(" && ").join(values!!)

    private class Serializer: HollowConditionSerializer<AndCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): AndCondition {
            val children = ArrayList<HollowCondition>()
            for (j in GsonHelper.getAsJsonArray(json, "values")) {
                if (!j.isJsonObject) throw JsonSyntaxException("And condition values must be an array of JsonObjects")
                children.add(HollowRecipeHelper.getCondition(j.asJsonObject))
            }
            return AndCondition(children.toTypedArray<HollowCondition>())
        }

        override fun toJson(json: JsonObject, value: AndCondition) {
            val values = JsonArray()
            value.values.forEach { values.add(HollowRecipeHelper.serialize(it)) }
            json.add("values", values)
        }

    }

    companion object {
        @JvmField
        val Id = "and".rl

        @JvmField
        val Serializer: HollowConditionSerializer<AndCondition> = Serializer()
    }
}