package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.internal.NamedValueEncoder
import net.minecraft.item.crafting.Ingredient
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueTagEncoder : NamedValueEncoder(), ICanEncodeTag {
    final override fun encodeTag(tag: INBT) = encodeTaggedTag(popTag(), tag)
    abstract fun encodeTaggedTag(key: String, tag: INBT)
}

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueNbtDecoder : NamedValueDecoder(), ICanDecodeTag {
    final override fun decodeTag(): INBT = decodeTaggedTag(popTag())
    abstract fun decodeTaggedTag(key: String): INBT
}

internal interface ICanEncodeTag : ICanEncodeCompoundNBT {
    fun encodeTag(tag: INBT)
    override fun encodeCompoundNBT(tag: CompoundNBT) = encodeTag(tag)
}

internal interface ICanDecodeTag : ICanDecodeCompoundNBT {
    fun decodeTag(): INBT
    override fun decodeCompoundNBT(): CompoundNBT = decodeTag() as CompoundNBT
}

internal interface ICanEncodeCompoundNBT {
    fun encodeCompoundNBT(tag: CompoundNBT)
}

internal interface ICanDecodeCompoundNBT {
    fun decodeCompoundNBT(): CompoundNBT
}

internal interface ICanEncodeIngredient{
    fun encodeIngredient(ingredient: Ingredient)
}

interface ICanDecodeIngredient{
    fun decodeIngredient() : Ingredient
}