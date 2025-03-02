package ru.hollowhorizon.hc.common.objects.recipe

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

interface HollowIngredientSerializer<T: HollowIngredient> {
    val id: ResourceLocation

    fun fromJson(json: JsonObject): T

    fun toJson(json: JsonObject, ingredient: T)

    fun fromNetwork(buf: FriendlyByteBuf): T

    fun toNetwork(buf: FriendlyByteBuf, ingredient: T)
}