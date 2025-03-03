package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.gson.JsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.rl

class ItemExistsCondition(private val item: ResourceLocation): HollowCondition {
    constructor(id: String): this(id.rl)
    constructor(namespace: String, path: String): this("$namespace:$path")

    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean = BuiltInRegistries.ITEM.containsKey(item)

    override fun toString(): String = "item_exists(\"$item\")"

    private class Serializer: HollowConditionSerializer<ItemExistsCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): ItemExistsCondition =
            ItemExistsCondition(GsonHelper.getAsString(json, "item").rl)

        override fun toJson(json: JsonObject, value: ItemExistsCondition) {
            json.addProperty("item", value.item.toString())
        }
    }

    companion object {
        @JvmField
        val Id = "item_exists".rl

        @JvmField
        val Serializer: HollowConditionSerializer<ItemExistsCondition> = Serializer()
    }
}
