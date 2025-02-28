package ru.hollowhorizon.hc.common.objects.recipe

interface HollowCoreIngredient {
    val hollowIngredient: IHollowIngredient? get() = null

    val requireTesting: Boolean get() = this.hollowIngredient != null && this.hollowIngredient!!.requireTesting()
}