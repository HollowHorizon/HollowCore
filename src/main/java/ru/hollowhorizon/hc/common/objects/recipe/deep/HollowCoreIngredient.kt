package ru.hollowhorizon.hc.common.objects.recipe.deep

import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.common.objects.recipe.ingredient.HollowIngredient

interface HollowCoreIngredient {
    val hollowIngredient: HollowIngredient? get() = null

    val requireTesting: Boolean get() = this.hollowIngredient != null && this.hollowIngredient!!.requireTesting()
}

val Ingredient.requireTesting
    get() = (this as HollowCoreIngredient).requireTesting