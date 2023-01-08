package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import net.minecraft.nbt.*
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.full.createType

internal val TagModule = SerializersModule {
    polymorphic(INBT::class) {
        subclass(ByteNBT::class, ForByteNBT)
        subclass(ShortNBT::class, ForShortNBT)
        subclass(IntNBT::class, ForIntNBT)
        subclass(LongNBT::class, ForLongNBT)
        subclass(FloatNBT::class, ForFloatNBT)
        subclass(DoubleNBT::class, ForDoubleNBT)
        subclass(StringNBT::class, ForStringNBT)
        subclass(EndNBT::class, ForNbtNull)
        subclass(ByteArrayNBT::class, ForByteArrayNBT)
        subclass(IntArrayNBT::class, ForIntArrayNBT)
        subclass(LongArrayNBT::class, ForLongArrayNBT)
        subclass(ListNBT::class, ForNbtList)
        subclass(CompoundNBT::class, ForCompoundNBT)
    }
}

@OptIn(ExperimentalSerializationApi::class)
sealed class NBTFormat(context: SerializersModule = EmptySerializersModule()) : SerialFormat {
    override val serializersModule = context + TagModule

    companion object Default : NBTFormat()

    @Serializable
    data class Initializator(val value: String)

    fun init() {
        //Первый вызов сериализатора происходит около 5 секунд, так что лучше сделать это заранее
        deserialize<Initializator>(serialize(Initializator("")))
    }

    fun <T> serialize(serializer: SerializationStrategy<T>, obj: T): INBT {
        return writeNbt(obj, serializer)
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, tag: INBT): T {
        return readNbt(tag, deserializer)
    }
}

internal const val NbtFormatNull = 1.toByte()

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <T, R1 : T, R2 : T> NBTFormat.selectMapMode(
    mapDescriptor: SerialDescriptor,
    ifMap: () -> R1,
    ifList: () -> R2
): T {
    val keyDescriptor = mapDescriptor.getElementDescriptor(0)
    val keyKind = keyDescriptor.kind
    return if (keyKind is PrimitiveKind || keyKind == SerialKind.ENUM) {
        ifMap()
    } else {
        ifList()
    }
}

fun INBT.save(stream: OutputStream) {
    if (this is CompoundNBT) {
        CompressedStreamTools.writeCompressed(this, stream)
    } else {
        CompressedStreamTools.writeCompressed(CompoundNBT().apply { put("nbt", this) }, stream)
    }
}

fun InputStream.loadAsNBT(): INBT {
    return CompressedStreamTools.readCompressed(this)
}

inline fun <reified T> NBTFormat.serialize(value: T): INBT {
    return serialize(serializersModule.serializer(), value)
}

fun <T> NBTFormat.serializeNoInline(value: T, cl: Class<T>): INBT {
    val c = cl as Class<*>
    return serialize(serializersModule.serializer(c.kotlin.createType()), value)
}

inline fun <reified T> NBTFormat.deserialize(tag: INBT): T {
    return deserialize(serializersModule.serializer(), tag)
}

fun <T : Any> NBTFormat.deserializeNoInline(tag: INBT, cl: Class<out T>): T {
    return deserialize(serializersModule.serializer(cl.kotlin.createType()), tag) as T
}

@OptIn(ExperimentalSerializationApi::class)
internal fun compoundTagInvalidKeyKind(keyDescriptor: SerialDescriptor) = IllegalStateException(
    "Value of type ${keyDescriptor.serialName} can't be used in a compound tag as map key. " +
            "It should have either primitive or enum kind, but its kind is ${keyDescriptor.kind}."
)

