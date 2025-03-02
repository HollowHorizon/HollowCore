package ru.hollowhorizon.hc.common.objects.recipe.deep

import io.netty.channel.ChannelHandler
import net.minecraft.network.PacketEncoder
import net.minecraft.resources.ResourceLocation

interface SupportedIngredientsPacketEncoder {
    fun hc_SetSupportedIngredients(value: Set<ResourceLocation>)
}

fun ChannelHandler.setSupportedIngredients(value: Set<ResourceLocation>) = (this as SupportedIngredientsPacketEncoder).hc_SetSupportedIngredients(value)
