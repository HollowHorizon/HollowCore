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

class OrCondition(private val children: Array<HollowCondition>): HollowCondition {
    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean {
        children.forEach { if (it.test(context)) return true }
        return false
    }

    override fun toString(): String = Joiner.on(" || ").join(children)

    private class Serializer: HollowConditionSerializer<OrCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): OrCondition {
            val children = ArrayList<HollowCondition>()
            for (j in GsonHelper.getAsJsonArray(json, "values")) {
                if (!j.isJsonObject) throw JsonSyntaxException("Or condition values must be an array of JsonObjects")
                children.add(HollowRecipeHelper.getCondition(j.asJsonObject))
            }
            return OrCondition(children.toTypedArray())
        }

        override fun toJson(json: JsonObject, value: OrCondition) {
            val values = JsonArray()
            value.children.forEach { values.add(HollowRecipeHelper.serialize(it)) }
            json.add("values", values)
        }
    }

    companion object {
        @JvmField
        val Id = "or".rl

        @JvmField
        val Serializer: HollowConditionSerializer<OrCondition> = Serializer()
    }
}
