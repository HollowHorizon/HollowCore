package ru.hollowhorizon.hc.common.objects.recipe

import ru.hollowhorizon.hc.common.objects.recipe.builtin.ArrayIngredient
import ru.hollowhorizon.hc.common.objects.recipe.builtin.DifferenceIngredient
import ru.hollowhorizon.hc.common.objects.recipe.builtin.NBTIngredient

object HollowCoreIngredientInitializer {
    @JvmStatic
    fun init() {
        HollowRecipeHelper.register(NBTIngredient.Serializer)
        HollowRecipeHelper.register(ArrayIngredient.AllIngredient.SERIALIZER)
        HollowRecipeHelper.register(ArrayIngredient.AnyIngredient.SERIALIZER)
        HollowRecipeHelper.register(DifferenceIngredient.SERIALIZER)
    }
}