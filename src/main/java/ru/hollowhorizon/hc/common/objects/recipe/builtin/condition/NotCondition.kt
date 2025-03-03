package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.common.objects.recipe.HollowRecipeHelper
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.rl

class NotCondition(private val child: HollowCondition): HollowCondition {
    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean = !child.test(context)

    override fun toString(): String = "!$child"

    private class Serializer: HollowConditionSerializer<NotCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): NotCondition =
            NotCondition(HollowRecipeHelper.getCondition(GsonHelper.getAsJsonObject(json, "value")))

        override fun toJson(json: JsonObject, value: NotCondition) {
            json.add("value", HollowRecipeHelper.serialize(value.child))
        }
    }

    companion object {
        @JvmField
        val Id = "not".rl

        @JvmField
        val Serializer: HollowConditionSerializer<NotCondition> = Serializer()
    }
}