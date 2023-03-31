package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.ByteNBT
import net.minecraft.nbt.CollectionNBT
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.NumberNBT
import net.minecraft.nbt.StringNBT

internal fun <T> NBTFormat.readNbt(element: INBT, deserializer: DeserializationStrategy<T>): T {
    val input = when (element) {
        is CompoundNBT -> NBTReader(this, element)
        is CollectionNBT<*> -> TagListDecoder(this, element)
        else -> TagPrimitiveReader(this, element)
    }
    return input.decodeSerializableValue(deserializer)
}

internal inline fun <reified T : INBT> cast(obj: INBT): T {
    check(obj is T) { "Expected ${T::class} but found ${obj::class}" }
    return obj
}

private inline fun <reified T> Any.cast() = this as T

@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractNBTReader(val format: NBTFormat, open val map: INBT) : NamedValueNbtDecoder() {

    override val serializersModule: SerializersModule
        get() = format.serializersModule


    private fun currentObject() = currentTagOrNull?.let { currentElement(it) } ?: map


    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val currentObject = currentObject()
        return when (descriptor.kind) {
            StructureKind.LIST -> {
                if (descriptor.kind == StructureKind.LIST && descriptor.getElementDescriptor(0).isNullable) NullableListDecoder(
                    format,
                    cast(currentObject)
                )
                else TagListDecoder(format, cast(currentObject))
            }
            is PolymorphicKind -> NbtMapDecoder(format, cast(currentObject))
            StructureKind.MAP -> selectMapMode(
                descriptor,
                { NbtMapDecoder(format, cast(currentObject)) }
            ) { TagListDecoder(format, cast(currentObject)) }

            else -> NBTReader(format, cast(currentObject))
        }
    }

    protected open fun getValue(tag: String): INBT {
        return currentElement(tag)
    }

    protected abstract fun currentElement(tag: String): INBT

    override fun decodeTaggedChar(tag: String): Char {
        val o = getValue(tag)
        val str = o.asString
        return if (str.length == 1) str[0] else throw IllegalStateException("$o can't be represented as Char")
    }

    override fun decodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(getValue(tag).asString)

    override fun decodeTaggedNull(tag: String): Nothing? {
        return null
    }

    override fun decodeTaggedNotNullMark(tag: String): Boolean {
        // If we don't do this assigment it fails. I have no clue why. This is a quantum bug, it cannot be debugged.
        val byteValue = (currentElement(tag) as? ByteNBT)?.asByte
        return byteValue != NbtFormatNull
    }

    override fun decodeTaggedBoolean(tag: String): Boolean = decodeTaggedByte(tag) == 1.toByte()
    override fun decodeTaggedByte(tag: String): Byte = getNumberValue(tag, { asByte }, { toByte() })
    override fun decodeTaggedShort(tag: String) = getNumberValue(tag, { asShort }, { toShort() })
    override fun decodeTaggedInt(tag: String): Int = getNumberValue(tag, { asInt }, { toInt() })

    override fun decodeTaggedLong(tag: String) = getNumberValue(tag, { asLong }, { toLong() })
    override fun decodeTaggedFloat(tag: String) = getNumberValue(tag, { asFloat }, { toFloat() })
    override fun decodeTaggedDouble(tag: String) = getNumberValue(tag, { asDouble }, { toDouble() })
    override fun decodeTaggedString(tag: String): String = getValue(tag).cast<StringNBT>().asString

    override fun decodeTaggedTag(key: String): INBT = getValue(key)

    private inline fun <T> getNumberValue(
        tag: String,
        getter: NumberNBT.() -> T,
        stringGetter: String.() -> T
    ): T {
        val value = getValue(tag)
        return if (value is NumberNBT) value.getter()
        else value.cast<StringNBT>().asString.stringGetter()
    }
}

private open class NBTReader(json: NBTFormat, override val map: CompoundNBT) : AbstractNBTReader(json, map) {
    private var position = 0

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < descriptor.elementsCount) {
            val name = descriptor.getTag(position++)
            if (map.contains(name)) {
                return position - 1
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): INBT = map.get(tag)!!

}

private class NbtMapDecoder(json: NBTFormat, override val map: CompoundNBT) : NBTReader(json, map) {
    private val keys = map.allKeys.toList()
    private val size: Int = keys.size * 2
    private var position = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String {
        val i = index / 2
        return keys[i]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): INBT {
        return if (position % 2 == 0) StringNBT.valueOf(tag) else map.get(tag)!!
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class NullableListDecoder(json: NBTFormat, override val map: CompoundNBT) : NBTReader(json, map) {
    private val size: Int = map.size()
    private var position = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): INBT {
        return map[tag]!!
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class TagListDecoder(json: NBTFormat, override val map: CollectionNBT<*>) : AbstractNBTReader(json, map) {
    private val size = map.size
    private var currentIndex = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = (index).toString()

    override fun currentElement(tag: String): INBT {
        return map[tag.toInt()]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size - 1) {
            currentIndex++
            return currentIndex
        }
        return CompositeDecoder.DECODE_DONE
    }
}

internal const val PRIMITIVE_TAG = "primitive"

private class TagPrimitiveReader(json: NBTFormat, override val map: INBT) : AbstractNBTReader(json, map) {

    init {
        pushTag(PRIMITIVE_TAG)
    }
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int  = 0

    override fun currentElement(tag: String): INBT {
        require(tag === PRIMITIVE_TAG) { "This input can only handle primitives with '$PRIMITIVE_TAG' tag" }
        return map
    }
}