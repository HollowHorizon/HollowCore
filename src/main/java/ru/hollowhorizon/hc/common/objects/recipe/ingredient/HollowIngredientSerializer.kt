package ru.hollowhorizon.hc.common.objects.recipe.ingredient

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A serializer interface for [HollowIngredient] implementations.
 * Provides methods to handle serialization and deserialization of ingredient data
 * between JSON and network formats.
 *
 * @param T the specific type of [HollowIngredient] handled by this serializer
 */
interface HollowIngredientSerializer<T: HollowIngredient> {
    /**
     * Gets the unique identifier for this serializer.
     *
     * @return the [ResourceLocation] representing the serializer ID
     */
    val id: ResourceLocation

    /**
     * Deserializes a [HollowIngredient] from a JSON object.
     *
     * @param json the JSON object containing ingredient data
     * @return the deserialized [HollowIngredient] instance
     */
    fun fromJson(json: JsonObject): T

    /**
     * Serializes a [HollowIngredient] into a JSON object.
     *
     * @param json the JSON object to populate with serialized data
     * @param ingredient the [HollowIngredient] instance to serialize
     */
    fun toJson(json: JsonObject, ingredient: T)

    /**
     * Deserializes a [HollowIngredient] from network data.
     *
     * @param buf the [FriendlyByteBuf] containing ingredient data
     * @return the deserialized [HollowIngredient] instance
     */
    fun fromNetwork(buf: FriendlyByteBuf): T

    /**
     * Serializes a [HollowIngredient] to network data.
     *
     * @param buf the [FriendlyByteBuf] to write serialized data to
     * @param ingredient the [HollowIngredient] instance to serialize
     */
    fun toNetwork(buf: FriendlyByteBuf, ingredient: T)
}