package ru.hollowhorizon.hc.common.objects.recipe

import net.minecraft.resources.ResourceLocation

object HollowRecipeHelper {
    @JvmStatic
    fun registerIngredientSerializer(serializer: HollowIngredientSerializer<*>) =
        DefaultHollowIngredient.registerSerializer(serializer)

    @JvmStatic
    fun getIngredientSerializer(id: ResourceLocation) = DefaultHollowIngredient.getSerializer(id)
}