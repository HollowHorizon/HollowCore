package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.rl

abstract class BooleanCondition private constructor(val value: Boolean): HollowCondition {
    override val id: ResourceLocation = "$value".rl

    override fun test(context: HollowCondition.ConditionContext): Boolean = value

    override fun toString(): String = "$value"

    class True: BooleanCondition(true) {
        companion object {
            @JvmField
            val Serializer: HollowConditionSerializer<True> = Serializer(::True)
        }
    }

    class False: BooleanCondition(false) {
        companion object {
            val Serializer: HollowConditionSerializer<False> = Serializer(::False)
        }
    }

    private class Serializer<T: BooleanCondition>(cond: () -> T): HollowConditionSerializer<T> {
        private val instance = cond()

        override val id: ResourceLocation = instance.id

        override fun fromJson(json: JsonObject): T = instance

        override fun toJson(json: JsonObject, value: T) {}
    }
}
