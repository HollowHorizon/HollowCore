package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*

internal fun <T> NBTFormat.writeNbt(value: T, serializer: SerializationStrategy<T>): INBT {
    lateinit var result: INBT

    if(value == null) return EndNBT.INSTANCE

    if(isPrimitiveType(value)) {
        when(value) {
            is Byte -> result = ByteNBT.valueOf(value)
            is Short -> result = ShortNBT.valueOf(value)
            is Int -> result = IntNBT.valueOf(value)
            is Long -> result = LongNBT.valueOf(value)
            is Float -> result = FloatNBT.valueOf(value)
            is Double -> result = DoubleNBT.valueOf(value)
            is Boolean -> result = ByteNBT.valueOf(value)
            is Char -> result = StringNBT.valueOf(value.toString())
            is String -> result = StringNBT.valueOf(value)
        }
        return result
    }

    val encoder = NBTWriter(this) { result = it }
    encoder.encodeSerializableValue(serializer, value)
    return result
}

fun <T> isPrimitiveType(value: T): Boolean {
    return when(value) {
        is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean, is Char, is String -> true
        else -> false
    }
}

@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractNBTWriter(
    val format: NBTFormat,
    val nodeConsumer: (INBT) -> Unit
) : NamedValueTagEncoder() {

    final override val serializersModule: SerializersModule
        get() = format.serializersModule


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: INBT)
    abstract fun getCurrent(): INBT

    override fun encodeTaggedNull(tag: String) = putElement(tag, ByteNBT.valueOf(NbtFormatNull))

    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, IntNBT.valueOf(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, ByteNBT.valueOf(value))
    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, ShortNBT.valueOf(value))
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, LongNBT.valueOf(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, FloatNBT.valueOf(value))
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, DoubleNBT.valueOf(value))
    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(tag, ByteNBT.valueOf(value))
    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, StringNBT.valueOf(value.toString()))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, StringNBT.valueOf(value))
    override fun encodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor, ordinal: Int) =
        putElement(tag, StringNBT.valueOf(enumDescriptor.getElementName(ordinal)))

    override fun encodeTaggedTag(key: String, tag: INBT) = putElement(key, tag)

    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, StringNBT.valueOf(value.toString()))
    }

    override fun elementName(descriptor: SerialDescriptor, index: Int): String {
        return if (descriptor.kind is PolymorphicKind) index.toString() else super.elementName(descriptor, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val consumer = if (currentTagOrNull == null) nodeConsumer
        else { node -> putElement(currentTag, node) }

        val encoder = when (descriptor.kind) {
            StructureKind.LIST -> {
                if (descriptor.kind == StructureKind.LIST && descriptor.getElementDescriptor(0).isNullable) NullableListEncoder(
                    format,
                    consumer
                )
                else NbtListEncoder(format, consumer)
            }

            is PolymorphicKind -> NbtMapEncoder(format, consumer)
            StructureKind.MAP -> selectMapMode(descriptor,
                ifMap = { NbtMapEncoder(format, consumer) }
            ) { NbtListEncoder(format, consumer) }

            else -> NBTWriter(format, consumer)
        }

        if (writePolymorphic) {
            writePolymorphic = false
            encoder.putElement("type", StringNBT.valueOf(descriptor.serialName))
        }

        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }
}

private open class NBTWriter(format: NBTFormat, nodeConsumer: (INBT) -> Unit) :
    AbstractNBTWriter(format, nodeConsumer) {

    protected val content: CompoundNBT = CompoundNBT()

    override fun putElement(key: String, element: INBT) {
        content.put(key, element)
    }

    override fun getCurrent(): INBT = content
}

private class NbtMapEncoder(format: NBTFormat, nodeConsumer: (INBT) -> Unit) : NBTWriter(format, nodeConsumer) {
    private lateinit var key: String

    override fun putElement(key: String, element: INBT) {
        val idx = key.toInt()
        // writing key
        when {
            idx % 2 == 0 -> this.key = when (element) {
                is CompoundNBT, is CollectionNBT<*>, is EndNBT -> throw compoundTagInvalidKeyKind(
                    when (element) {
                        is CompoundNBT -> ForCompoundNBT.descriptor
                        is CollectionNBT<*> -> ForNbtList.descriptor
                        is EndNBT -> ForNbtNull.descriptor
                        else -> error("impossible")
                    }
                )

                else -> element.asString
            }

            else -> content.put(this.key, element)
        }
    }

    override fun getCurrent(): INBT = content

}

private class NullableListEncoder(format: NBTFormat, nodeConsumer: (INBT) -> Unit) : NBTWriter(format, nodeConsumer) {
    override fun putElement(key: String, element: INBT) {
        content.put(key, element)
    }

    override fun getCurrent(): INBT = content

}

private fun ListNBT.addAnyTag(index: Int, tag: INBT) {
    this.list.add(index, tag)
}

private class NbtListEncoder(json: NBTFormat, nodeConsumer: (INBT) -> Unit) :
    AbstractNBTWriter(json, nodeConsumer) {
    private val list: ListNBT = ListNBT()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun putElement(key: String, element: INBT) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): INBT = list
}