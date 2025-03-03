package ru.hollowhorizon.hc.common.objects.recipe.ingredient

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.common.utils.JavaHacks
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowCoreIngredient
import ru.hollowhorizon.hc.common.objects.recipe.packet.HollowIngredientPacketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

class DefaultHollowIngredient(override val hollowIngredient: HollowIngredient) : Ingredient(Stream.empty()),
    HollowCoreIngredient {
    companion object {
        internal val registeredSerializers: Map<ResourceLocation, HollowIngredientSerializer<*>> = ConcurrentHashMap()
        val typeKey = "hollowcore:type"
        val packetMarker = -1

        @JvmStatic
        internal fun registerSerializer(serializer: HollowIngredientSerializer<*>) {
            check((registeredSerializers as ConcurrentHashMap).putIfAbsent(serializer.id, serializer) == null) {
                "Hollow Serializer ${serializer.id} already registered"
            }
        }

        @JvmStatic
        internal fun getSerializer(id: ResourceLocation): HollowIngredientSerializer<*>? = registeredSerializers[id]
    }

    override val requireTesting: Boolean
        get() = hollowIngredient.requireTesting()

    override fun getItems(): Array<ItemStack> {
        if (this.itemStacks == null) {
            this.itemStacks = hollowIngredient.items.toTypedArray()
        }

        return this.itemStacks!!
    }

    override fun test(stack: ItemStack?): Boolean = stack != null && this.hollowIngredient.test(stack)

    override fun toNetwork(buffer: FriendlyByteBuf) {
        val singr = HollowIngredientPacketHandler.SUPPORTED_INGREDIENTS.get()

        if (singr != null && !singr.contains(hollowIngredient.serializer.id))
            super.toNetwork(buffer)
        else {
            buffer.writeVarInt(packetMarker)
            buffer.writeResourceLocation(hollowIngredient.serializer.id)
            hollowIngredient.serializer.toNetwork(buffer, JavaHacks.forceCast(hollowIngredient))
        }
    }

    override fun toJson(): JsonElement {
        val json = JsonObject()
        json.addProperty(typeKey, hollowIngredient.serializer.id.toString())
        hollowIngredient.serializer.toJson(json, JavaHacks.forceCast(hollowIngredient))
        return super.toJson()
    }
}
