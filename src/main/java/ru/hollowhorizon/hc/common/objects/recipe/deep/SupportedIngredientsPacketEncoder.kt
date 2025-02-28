package ru.hollowhorizon.hc.common.objects.recipe.deep

import net.minecraft.resources.ResourceLocation

interface SupportedIngredientsPacketEncoder {
    fun hcSetSupportedIngredients(value: Set<ResourceLocation>)
}