package ru.hollowhorizon.hc.common.objects.recipe.condition

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation

interface HollowConditionSerializer<T: HollowCondition> {
    val id: ResourceLocation

    fun fromJson(json: JsonObject): T

    fun toJson(json: JsonObject, value: T)

    fun getJson(value: T): JsonObject {
        val json = JsonObject()
        this.toJson(json, value)
        json.addProperty("hollowcore:type", value.id.toString())
        return json
    }
}