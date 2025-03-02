package ru.hollowhorizon.hc.common.objects.recipe

import ru.hollowhorizon.hc.common.objects.recipe.builtin.NBTIngredient

object HollowCoreIngredientInitializer {
    @JvmStatic
    fun init() {
        HollowRecipeHelper.registerIngredientSerializer(NBTIngredient.Serializer)
    }
}