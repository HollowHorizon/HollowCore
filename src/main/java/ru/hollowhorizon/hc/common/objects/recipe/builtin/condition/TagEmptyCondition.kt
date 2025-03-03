package ru.hollowhorizon.hc.common.objects.recipe.builtin.condition

import com.google.gson.JsonObject
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowConditionSerializer
import ru.hollowhorizon.hc.common.utils.rl

class TagEmptyCondition(tag: ResourceLocation): HollowCondition {
    constructor(location: String): this(location.rl)
    constructor(namespace: String, path: String): this("$namespace:$path")

    val tag = TagKey.create(Registries.ITEM, tag)

    override val id: ResourceLocation = Id

    override fun test(context: HollowCondition.ConditionContext): Boolean = context.getTag(tag).isEmpty()

    override fun toString(): String = "tag_empty(\"${tag.location}\")"

    private class Serializer: HollowConditionSerializer<TagEmptyCondition> {
        override val id: ResourceLocation = Id

        override fun fromJson(json: JsonObject): TagEmptyCondition =
            TagEmptyCondition(GsonHelper.getAsString(json, "tag"))

        override fun toJson(json: JsonObject, value: TagEmptyCondition) {
            json.addProperty("tag", value.tag.location.toString())
        }
    }

    companion object {
        @JvmField
        val Id = "tag_empty".rl

        @JvmField
        val Serializer: HollowConditionSerializer<TagEmptyCondition> = Serializer()
    }
}