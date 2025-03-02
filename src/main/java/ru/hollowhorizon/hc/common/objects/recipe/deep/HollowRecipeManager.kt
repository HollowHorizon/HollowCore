package ru.hollowhorizon.hc.common.objects.recipe.deep

import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition

interface HollowRecipeManager {
    var hc_context: HollowCondition.ConditionContext
}