package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.internal.NamedValueEncoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueTagEncoder : NamedValueEncoder(), ICanEncodeTag {
    final override fun encodeTag(tag: Tag) = encodeTaggedTag(popTag(), tag)
    abstract fun encodeTaggedTag(key: String, tag: Tag)
}

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueNbtDecoder : NamedValueDecoder(), ICanDecodeTag {
    final override fun decodeTag(): Tag = decodeTaggedTag(popTag())
    abstract fun decodeTaggedTag(key: String): Tag
}

internal interface ICanEncodeTag : ICanEncodeCompoundNBT {
    fun encodeTag(tag: Tag)
    override fun encodeCompoundNBT(tag: Tag) = encodeTag(tag)
}

internal interface ICanDecodeTag : ICanDecodeCompoundNBT {
    fun decodeTag(): Tag
    override fun decodeCompoundNBT(): CompoundTag = decodeTag() as CompoundTag
}

internal interface ICanEncodeCompoundNBT {
    fun encodeCompoundNBT(tag: Tag)
}

internal interface ICanDecodeCompoundNBT {
    fun decodeCompoundNBT(): CompoundTag
}

internal interface ICanEncodeIngredient {
    fun encodeIngredient(ingredient: Tag)
}

interface ICanDecodeIngredient {
    fun decodeIngredient(): Tag
}