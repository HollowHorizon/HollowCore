package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*
import ru.hollowhorizon.hc.mixin.ListTagAccessor

internal fun <T> NBTFormat.writeNbt(value: T, serializer: SerializationStrategy<T>): Tag {
    lateinit var result: Tag

    if (value == null) return EndTag.INSTANCE

    if (isPrimitiveType(value)) {
        when (value) {
            is Byte -> result = ByteTag.valueOf(value)
            is Short -> result = ShortTag.valueOf(value)
            is Int -> result = IntTag.valueOf(value)
            is Long -> result = LongTag.valueOf(value)
            is Float -> result = FloatTag.valueOf(value)
            is Double -> result = DoubleTag.valueOf(value)
            is Boolean -> result = ByteTag.valueOf(value)
            is Char -> result = StringTag.valueOf(value.toString())
            is String -> result = StringTag.valueOf(value)
        }
        return result
    }

    val encoder = NBTWriter(this) { result = it }
    encoder.encodeSerializableValue(serializer, value)
    return result
}

fun <T> isPrimitiveType(value: T): Boolean {
    return when (value) {
        is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean, is Char, is String -> true
        else -> false
    }
}

@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractNBTWriter(
    val format: NBTFormat,
    val nodeConsumer: (Tag) -> Unit,
) : NamedValueTagEncoder() {

    final override val serializersModule: SerializersModule
        get() = format.serializersModule


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: Tag)
    abstract fun getCurrent(): Tag

    override fun encodeTaggedNull(tag: String) = putElement(tag, ByteTag.valueOf(NbtFormatNull))

    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, IntTag.valueOf(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, ByteTag.valueOf(value))
    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, ShortTag.valueOf(value))
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, LongTag.valueOf(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, FloatTag.valueOf(value))
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, DoubleTag.valueOf(value))
    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(tag, ByteTag.valueOf(value))
    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, StringTag.valueOf(value.toString()))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, StringTag.valueOf(value))
    override fun encodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor, ordinal: Int) =
        putElement(tag, StringTag.valueOf(enumDescriptor.getElementName(ordinal)))

    override fun encodeTaggedTag(key: String, tag: Tag) = putElement(key, tag)

    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, StringTag.valueOf(value.toString()))
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
            encoder.putElement("type", StringTag.valueOf(descriptor.serialName))
        }

        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }
}

private open class NBTWriter(format: NBTFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractNBTWriter(format, nodeConsumer) {

    protected val content: CompoundTag = CompoundTag()

    override fun putElement(key: String, element: Tag) {
        content.put(key, element)
    }

    override fun getCurrent(): Tag = content
}

private class NbtMapEncoder(format: NBTFormat, nodeConsumer: (Tag) -> Unit) : NBTWriter(format, nodeConsumer) {
    private lateinit var key: String

    override fun putElement(key: String, element: Tag) {
        val idx = key.toInt()
        // writing key
        when {
            idx % 2 == 0 -> this.key = when (element) {
                is CompoundTag, is CollectionTag<*>, is EndTag -> throw compoundTagInvalidKeyKind(
                    when (element) {
                        is CompoundTag -> ForCompoundNBT.descriptor
                        is CollectionTag<*> -> ForNbtList.descriptor
                        is EndTag -> ForNbtNull.descriptor
                        else -> error("impossible")
                    }
                )

                else -> element.asString
            }

            else -> content.put(this.key, element)
        }
    }

    override fun getCurrent(): Tag = content

}

private class NullableListEncoder(format: NBTFormat, nodeConsumer: (Tag) -> Unit) : NBTWriter(format, nodeConsumer) {
    override fun putElement(key: String, element: Tag) {
        content.put(key, element)
    }

    override fun getCurrent(): Tag = content

}

private fun ListTag.addAnyTag(index: Int, tag: Tag) {
    (this as? ListTagAccessor)?.list()?.add(index, tag) ?: this.addTag(index, tag)
}

private class NbtListEncoder(json: NBTFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractNBTWriter(json, nodeConsumer) {
    private val list: ListTag = ListTag()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun putElement(key: String, element: Tag) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): Tag = list
}