package ru.hollowhorizon.hc.common.objects.recipe

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import ru.hollowhorizon.hc.client.utils.JavaHacks
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

class HollowIngredient(override val hollowIngredient: IHollowIngredient) : Ingredient(Stream.empty()), HollowCoreIngredient {
    companion object {
        private val registeredSerializers: Map<ResourceLocation, IHollowIngredientSerializer<*>> = ConcurrentHashMap()
        val typeKey = "hollowcore:type"
        val packetMarker = -1
        val supportedIngredients: ThreadLocal<Set<ResourceLocation>> = ThreadLocal()

        @JvmStatic
        fun registerSerializer(serializer: IHollowIngredientSerializer<*>) {
            check((registeredSerializers as ConcurrentHashMap).putIfAbsent(serializer.id, serializer) == null) {
                "Hollow Serializer ${serializer.id} already registered"
            }
        }

        @JvmStatic
        fun getSerializer(id: ResourceLocation): IHollowIngredientSerializer<*>? = registeredSerializers[id]
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
        val singr = supportedIngredients.get()

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
