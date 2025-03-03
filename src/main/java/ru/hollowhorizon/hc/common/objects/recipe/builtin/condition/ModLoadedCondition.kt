package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.ModList
import ru.hollowhorizon.hc.common.utils.rl

class ModLoadedCondition(private val modId: String): HollowCondition {
    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean = ModList.isLoaded(modId)

    override fun toString(): String = "mod_loaded(\"$modId\")"

    private class Serializer: HollowConditionSerializer<ModLoadedCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): ModLoadedCondition = ModLoadedCondition(GsonHelper.getAsString(json, "modid"))

        override fun toJson(json: JsonObject, value: ModLoadedCondition) {
            json.addProperty("modid", value.modId)
        }
    }

    companion object {
        @JvmField
        val Id = "mod_loaded".rl

        @JvmField
        val Serializer: HollowConditionSerializer<ModLoadedCondition> = Serializer()
    }
}