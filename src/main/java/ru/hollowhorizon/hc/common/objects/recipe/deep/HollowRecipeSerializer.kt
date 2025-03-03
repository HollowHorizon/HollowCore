package ru.hollowhorizon.hc.common.objects.recipe.deep

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition

interface HollowRecipeSerializer<T: Recipe<*>?> {
    fun fromJson(id: ResourceLocation, json: JsonObject, ctx: HollowCondition.ConditionContext): T? = (this as RecipeSerializer<T>).fromJson(id, json)
}
