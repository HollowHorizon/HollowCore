package ru.hollowhorizon.hc.common.objects.recipe

import ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient.ArrayIngredient
import ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient.DifferenceIngredient
import ru.hollowhorizon.hc.common.objects.recipe.builtin.ingredient.NBTIngredient
import ru.hollowhorizon.hc.common.objects.recipe.builtin.condition.*

object HollowCoreIngredientInitializer {
    @JvmStatic
    fun init() {
        // Ingredient types
        HollowRecipeHelper.register(NBTIngredient.Serializer)
        HollowRecipeHelper.register(ArrayIngredient.AllIngredient.Serializer)
        HollowRecipeHelper.register(ArrayIngredient.AnyIngredient.Serializer)
        HollowRecipeHelper.register(DifferenceIngredient.Serializer)

        // Condition types
        HollowRecipeHelper.register(AndCondition.Serializer)
        HollowRecipeHelper.register(ItemExistsCondition.Serializer)
        HollowRecipeHelper.register(NotCondition.Serializer)
        HollowRecipeHelper.register(OrCondition.Serializer)
        HollowRecipeHelper.register(TagEmptyCondition.Serializer)
        HollowRecipeHelper.register(BooleanCondition.True.Serializer)
        HollowRecipeHelper.register(BooleanCondition.False.Serializer)
        HollowRecipeHelper.register(ModLoadedCondition.Serializer)
    }
}