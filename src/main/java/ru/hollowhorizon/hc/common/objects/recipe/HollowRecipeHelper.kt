package ru.hollowhorizon.hc.common.objects.recipe

import net.minecraft.resources.ResourceLocation

object HollowRecipeHelper {
    @JvmStatic
    fun registerIngredientSerializer(serializer: IHollowIngredientSerializer<*>) =
        HollowIngredient.registerSerializer(serializer)

    @JvmStatic
    fun getIngredientSerializer(id: ResourceLocation) = HollowIngredient.getSerializer(id)
}