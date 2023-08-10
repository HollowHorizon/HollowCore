package ru.hollowhorizon.hc.client.utils.nbt

import com.google.common.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.modules.*
import net.minecraft.nbt.*
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.capabilities.HollowCapability
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

internal val TagModule = SerializersModule {
    polymorphic(Tag::class) {
        subclass(ByteTag::class, ForByteNBT)
        subclass(ShortTag::class, ForShortNBT)
        subclass(IntTag::class, ForIntNBT)
        subclass(LongTag::class, ForLongNBT)
        subclass(FloatTag::class, ForFloatNBT)
        subclass(DoubleTag::class, ForDoubleNBT)
        subclass(StringTag::class, ForStringNBT)
        subclass(EndTag::class, ForNbtNull)
        subclass(ByteArrayTag::class, ForByteArrayNBT)
        subclass(IntArrayTag::class, ForIntArrayNBT)
        subclass(LongArrayTag::class, ForLongArrayNBT)
        subclass(ListTag::class, ForNbtList)
        subclass(CompoundTag::class, ForCompoundNBT)
    }
    contextual(ForBlockPos)
    contextual(ForResourceLocation)
    contextual(ForSoundEvent)
}

object CapabilityModule {

    @OptIn(InternalSerializationApi::class)
    @Suppress("unchecked_cast")
    fun build() = SerializersModule {
        polymorphic(HollowCapability::class) {
            fun <B : HollowCapability> subclass(c: Class<B>) {
                val klass = c.kotlin
                subclass(klass, klass.serializer())
            }

            HollowCapabilityStorageV2.capabilities.forEach {
                HollowCore.LOGGER.info("Registering capability serializer: {}", it.name)
                subclass(it as Class<HollowCapability>)
            }
        }
    }
}

val MAPPINGS_SERIALIZER by lazy { NBTFormat() }

open class NBTFormat(context: SerializersModule = EmptySerializersModule()) : SerialFormat {
    override val serializersModule = context + TagModule

    companion object Default : NBTFormat(CapabilityModule.build()) {
        init {
            HollowCore.LOGGER.info("Default Serializer loaded!")
        }
    }

    @Serializable
    data class Initializator(val value: String)

    fun init() {
        //Первый вызов сериализатора происходит около 5 секунд, так что лучше сделать это заранее и асинхронно
        runBlocking {
            deserialize<Initializator>(serialize(Initializator("")))
        }
    }

    fun <T> serialize(serializer: SerializationStrategy<T>, obj: T): Tag {
        return writeNbt(obj, serializer)
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, tag: Tag): T {
        return readNbt(tag, deserializer)
    }
}

@Serializable
class Test(val data: SerializableRunnable)

@Serializable
class SerializableRunnable(private val inner: java.io.Serializable) : Runnable, java.io.Serializable {

    override fun run() {
        (inner as? Runnable)?.run()
    }
}

fun main() {

    val test = Test(SerializableRunnable( {
        println("1")
        println("2")
        println("3")
    } as java.io.Serializable))

    test.data.run()
    val nbt = NBTFormat.serialize(test)
    println(nbt)
    val other: Test = NBTFormat.deserialize(nbt)
    other.data.run()
}

internal const val NbtFormatNull = 1.toByte()

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <T, R1 : T, R2 : T> selectMapMode(
    mapDescriptor: SerialDescriptor,
    ifMap: () -> R1,
    ifList: () -> R2,
): T {
    val keyDescriptor = mapDescriptor.getElementDescriptor(0)
    val keyKind = keyDescriptor.kind
    return if (keyKind is PrimitiveKind || keyKind == SerialKind.ENUM) {
        ifMap()
    } else {
        ifList()
    }
}

fun Tag.save(stream: DataOutputStream) {
    NbtIo.writeUnnamedTag(this, stream)
}

fun Tag.save(stream: OutputStream) = this.save(DataOutputStream(stream))

fun DataInputStream.loadAsNBT(): Tag {
    return NbtIo.read(this)
}

fun InputStream.loadAsNBT() = DataInputStream(this).loadAsNBT()

inline fun <reified T> NBTFormat.serialize(value: T): Tag {
    return serialize(serializersModule.serializer(), value)
}

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalSerializationApi::class)
fun <T : Any> NBTFormat.serializeNoInline(value: T, cl: Class<T>): Tag {
    val typeToken = TypeToken.of(cl)
    return serialize(serializersModule.serializer(typeToken.type), value)
}

inline fun <reified T> NBTFormat.deserialize(tag: Tag): T {
    return deserialize(serializersModule.serializer(), tag)
}

@Suppress("UnstableApiUsage", "UNCHECKED_CAST")
@OptIn(ExperimentalSerializationApi::class)
fun <T : Any> NBTFormat.deserializeNoInline(tag: Tag, cl: Class<out T>): T {
    val typeToken = TypeToken.of(cl)
    return deserialize(serializersModule.serializer(typeToken.type), tag) as T
}

@OptIn(ExperimentalSerializationApi::class)
internal fun compoundTagInvalidKeyKind(keyDescriptor: SerialDescriptor) = IllegalStateException(
    "Value of type ${keyDescriptor.serialName} can't be used in a compound tag as map key. " +
            "It should have either primitive or enum kind, but its kind is ${keyDescriptor.kind}."
)

