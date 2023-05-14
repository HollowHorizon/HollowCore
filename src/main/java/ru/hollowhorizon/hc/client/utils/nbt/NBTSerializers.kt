package ru.hollowhorizon.hc.client.utils.nbt

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3f
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl
import java.util.*

private inline fun <T> missingField(missingField: String, deserializing: String, defaultValue: () -> T): T {
    HollowCore.LOGGER.warn("Missing $missingField while deserializing $deserializing")
    return defaultValue()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = net.minecraft.util.math.BlockPos::class)
object ForBlockPos : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockPos", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: BlockPos) = encoder.encodeLong(value.asLong())
    override fun deserialize(decoder: Decoder): BlockPos = BlockPos.of(decoder.decodeLong())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ResourceLocation::class)
object ForResourceLocation : KSerializer<ResourceLocation> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ResourceLocation) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): ResourceLocation = ResourceLocation(decoder.decodeString())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SoundEvent::class)
object ForSoundEvent : KSerializer<SoundEvent> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SoundEvent", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SoundEvent) {
        encoder.encodeString(ForgeRegistries.SOUND_EVENTS.getKey(value).toString())
    }

    override fun deserialize(decoder: Decoder): SoundEvent = ForgeRegistries.SOUND_EVENTS.getValue(decoder.decodeString().rl)
        ?: SoundEvents.ITEM_PICKUP
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ByteNBT::class)
object ForByteNBT : KSerializer<ByteNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteNBT", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: ByteNBT) = encoder.encodeByte(value.asByte)
    override fun deserialize(decoder: Decoder): ByteNBT = ByteNBT.valueOf(decoder.decodeByte())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ShortNBT::class)
object ForShortNBT : KSerializer<ShortNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ShortNBT", PrimitiveKind.SHORT)
    override fun serialize(encoder: Encoder, value: ShortNBT) = encoder.encodeShort(value.asShort)
    override fun deserialize(decoder: Decoder): ShortNBT = ShortNBT.valueOf(decoder.decodeShort())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = IntNBT::class)
object ForIntNBT : KSerializer<IntNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntNBT", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: IntNBT) = encoder.encodeInt(value.asInt)
    override fun deserialize(decoder: Decoder): IntNBT = IntNBT.valueOf(decoder.decodeInt())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LongNBT::class)
object ForLongNBT : KSerializer<LongNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LongNBT", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LongNBT) = encoder.encodeLong(value.asLong)
    override fun deserialize(decoder: Decoder): LongNBT = LongNBT.valueOf(decoder.decodeLong())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = FloatNBT::class)
object ForFloatNBT : KSerializer<FloatNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FloatNBT", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: FloatNBT) = encoder.encodeFloat(value.asFloat)
    override fun deserialize(decoder: Decoder): FloatNBT = FloatNBT.valueOf(decoder.decodeFloat())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DoubleNBT::class)
object ForDoubleNBT : KSerializer<DoubleNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DoubleNBT", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: DoubleNBT) = encoder.encodeDouble(value.asDouble)
    override fun deserialize(decoder: Decoder): DoubleNBT = DoubleNBT.valueOf(decoder.decodeDouble())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = StringNBT::class)
object ForStringNBT : KSerializer<StringNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringNBT", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: StringNBT) = encoder.encodeString(value.asString)
    override fun deserialize(decoder: Decoder): StringNBT = StringNBT.valueOf(decoder.decodeString())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = EndNBT::class)
object ForNbtNull : KSerializer<EndNBT> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EndNBT", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: EndNBT) = encoder.encodeByte(0)
    override fun deserialize(decoder: Decoder): EndNBT = EndNBT.INSTANCE.also { decoder.decodeByte() }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ByteArrayNBT::class)
object ForByteArrayNBT : KSerializer<ByteArrayNBT> {
    override val descriptor: SerialDescriptor = PublicisedListLikeDescriptorImpl(ForByteNBT.descriptor, "ByteArrayNBT")

    override fun serialize(encoder: Encoder, value: ByteArrayNBT) =
        ListSerializer(ForByteNBT).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): ByteArrayNBT =
        ByteArrayNBT(ListSerializer(ForByteNBT).deserialize(decoder).map { it.asByte })
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = IntArrayNBT::class)
object ForIntArrayNBT : KSerializer<IntArrayNBT> {
    override val descriptor: SerialDescriptor = PublicisedListLikeDescriptorImpl(ForIntNBT.descriptor, "IntArrayNBT")

    override fun serialize(encoder: Encoder, value: IntArrayNBT) =
        ListSerializer(ForIntNBT).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): IntArrayNBT =
        IntArrayNBT(ListSerializer(ForIntNBT).deserialize(decoder).map { it.asInt })
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Matrix4f::class)
object ForMatrix4f : KSerializer<Matrix4f> {
    override val descriptor: SerialDescriptor = PublicisedListLikeDescriptorImpl(ForFloatNBT.descriptor, "Matrix4f")

    override fun serialize(encoder: Encoder, value: Matrix4f) =
        ListSerializer(ForFloatNBT).serialize(encoder, listOf(
            value.m00, value.m01, value.m02, value.m03,
            value.m10, value.m11, value.m12, value.m13,
            value.m20, value.m21, value.m22, value.m23,
            value.m30, value.m31, value.m32, value.m33
        ).map { FloatNBT.valueOf(it) })

    override fun deserialize(decoder: Decoder): Matrix4f =
        Matrix4f(ListSerializer(ForFloatNBT).deserialize(decoder).map { it.asFloat }.toFloatArray())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LongArrayNBT::class)
object ForLongArrayNBT : KSerializer<LongArrayNBT> {
    override val descriptor: SerialDescriptor = PublicisedListLikeDescriptorImpl(ForLongNBT.descriptor, "LongArrayNBT")

    override fun serialize(encoder: Encoder, value: LongArrayNBT) =
        ListSerializer(ForLongNBT).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): LongArrayNBT =
        LongArrayNBT(ListSerializer(ForLongNBT).deserialize(decoder).map { it.asLong })
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Serializer(forClass = INBT::class)
object ForTag : KSerializer<INBT> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("kotlinx.serialization.Polymorphic", PolymorphicKind.OPEN) {
            element("type", String.serializer().descriptor)
            element(
                "value",
                buildSerialDescriptor(
                    "kotlinx.serialization.Polymorphic<${INBT::class.simpleName}>",
                    SerialKind.CONTEXTUAL
                )
            )
        }

    override fun serialize(encoder: Encoder, value: INBT) {
        if (encoder is ICanEncodeTag) encoder.encodeTag(value)
        else PolymorphicSerializer(INBT::class).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): INBT {
        return if (decoder is ICanDecodeTag) decoder.decodeTag()
        else PolymorphicSerializer(INBT::class).deserialize(decoder)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ListNBT::class)
object ForNbtList : KSerializer<ListNBT> {
    override val descriptor: SerialDescriptor = PublicisedListLikeDescriptorImpl(ForTag.descriptor, "ListNBT")

    override fun serialize(encoder: Encoder, value: ListNBT) {
        ListSerializer(ForTag).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ListNBT = ListNBT().apply {
        for (tag in ListSerializer(ForTag).deserialize(decoder)) {
            add(tag)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = CompoundNBT::class)
object ForCompoundNBT : KSerializer<CompoundNBT> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NbtCompound") {
        mapSerialDescriptor(PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING), ForTag.descriptor)
    }

    override fun serialize(encoder: Encoder, value: CompoundNBT) {
        if (encoder is ICanEncodeCompoundNBT) {
            encoder.encodeCompoundNBT(value)
        } else {
            MapSerializer(String.serializer(), ForTag).serialize(
                encoder,
                value.allKeys.associateWith { value.get(it)!! }
            )
        }

    }

    override fun deserialize(decoder: Decoder): CompoundNBT {
        if (decoder is ICanDecodeCompoundNBT) {
            return decoder.decodeCompoundNBT()
        }
        return CompoundNBT().apply {
            for ((key, value) in MapSerializer(String.serializer(), ForTag).deserialize(decoder)) {
                put(key, value)
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ItemStack::class)
object ForItemStack : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ItemStack") {
        element("tag", ForCompoundNBT.descriptor)
    }


    override fun serialize(encoder: Encoder, value: ItemStack) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, ForCompoundNBT, CompoundNBT().apply { value.save(this) })
        }
    }


    override fun deserialize(decoder: Decoder): ItemStack {
        val dec = decoder.beginStructure(descriptor)

        var tag: CompoundNBT? = null
        if (dec.decodeSequentially()) {
            tag = dec.decodeSerializableElement(descriptor, 0, ForCompoundNBT)
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    0 -> tag = dec.decodeSerializableElement(descriptor, i, ForCompoundNBT)
                    CompositeDecoder.DECODE_DONE -> break@loop
                    else -> throw SerializationException("Unexpected index: $i")
                }
            }
        }
        dec.endStructure(descriptor)
        return ItemStack.of(tag!!)

    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = UUID::class)
object ForUuid : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Uuid") {
        element("most", Long.serializer().descriptor)
        element("least", Long.serializer().descriptor)
    }

    private const val MostIndex = 0
    private const val LeastIndex = 1

    override fun serialize(encoder: Encoder, value: UUID) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeLongElement(descriptor, MostIndex, value.mostSignificantBits)
        compositeOutput.encodeLongElement(descriptor, LeastIndex, value.leastSignificantBits)
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        val index = dec.decodeElementIndex(descriptor)
        return if (dec.decodeSequentially()) {
            val most = dec.decodeLongElement(descriptor, MostIndex)
            val least = dec.decodeLongElement(descriptor, LeastIndex)

            dec.endStructure(descriptor)
            UUID(most, least)
        } else {
            handleUnorthodoxInputOrdering(index, dec)
        }
    }

    private fun handleUnorthodoxInputOrdering(index: Int, dec: CompositeDecoder): UUID {
        var most: Long? = null // consider using flags or bit mask if you
        var least: Long? = null // need to read nullable non-optional properties
        when (index) {
            CompositeDecoder.DECODE_DONE -> throw SerializationException("Read should not be done yet.")
            MostIndex -> most = dec.decodeLongElement(descriptor, index)
            LeastIndex -> least = dec.decodeLongElement(descriptor, index)
            else -> throw SerializationException("Unknown index $index")
        }

        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                MostIndex -> most = dec.decodeLongElement(descriptor, i)
                LeastIndex -> least = dec.decodeLongElement(descriptor, i)
                else -> throw SerializationException("Unknown index $i")
            }
        }
        dec.endStructure(descriptor)
        return UUID(
            most ?: missingField("most", "UUID") { 0L },
            least ?: missingField("least", "UUID") { 0L }
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal sealed class PublicisedListLikeDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        if (index != 0) throw IllegalStateException("List descriptor has only one child element, index: $index")
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return elementDesc
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicisedListLikeDescriptor) return false
        if (elementDesc == other.elementDesc && serialName == other.serialName) return true
        return false
    }

    override fun hashCode(): Int {
        return elementDesc.hashCode() * 31 + serialName.hashCode()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Vector3d::class)
object ForVector3d : KSerializer<Vector3d> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vector3d") {
        element("x", Double.serializer().descriptor)
        element("y", Double.serializer().descriptor)
        element("z", Double.serializer().descriptor)
    }


    private const val XIndex = 0
    private const val YIndex = 1
    private const val ZIndex = 2

    override fun serialize(encoder: Encoder, value: Vector3d) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeDoubleElement(descriptor, XIndex, value.x)
        compositeOutput.encodeDoubleElement(descriptor, YIndex, value.y)
        compositeOutput.encodeDoubleElement(descriptor, ZIndex, value.z)
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Vector3d {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        var x = 0.0
        var y = 0.0
        var z = 0.0
        var xExists = false
        var yExists = false
        var zExists = false
        if (dec.decodeSequentially()) {
            x = dec.decodeDoubleElement(descriptor, XIndex)
            y = dec.decodeDoubleElement(descriptor, YIndex)
            z = dec.decodeDoubleElement(descriptor, ZIndex)
            xExists = true
            yExists = true
            zExists = true
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    XIndex -> {
                        x = dec.decodeDoubleElement(descriptor, i)
                        xExists = true
                    }

                    YIndex -> {
                        y = dec.decodeDoubleElement(descriptor, i)
                        yExists = true
                    }

                    ZIndex -> {
                        z = dec.decodeDoubleElement(descriptor, i)
                        zExists = true
                    }

                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }


        dec.endStructure(descriptor)
        if (!xExists) x = missingField("x", "Vec3d") { 0.0 }
        if (!yExists) y = missingField("y", "Vec3d") { 0.0 }
        if (!zExists) z = missingField("z", "Vec3d") { 0.0 }

        return Vector3d(x, y, z)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Vector3f::class)
object ForVector3f : KSerializer<Vector3f> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vector3f") {
        element("x", Float.serializer().descriptor)
        element("y", Float.serializer().descriptor)
        element("z", Float.serializer().descriptor)
    }
    private const val XIndex = 0
    private const val YIndex = 1
    private const val ZIndex = 2

    override fun serialize(encoder: Encoder, value: Vector3f) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeFloatElement(descriptor, XIndex, value.x())
        compositeOutput.encodeFloatElement(descriptor, YIndex, value.y())
        compositeOutput.encodeFloatElement(descriptor, ZIndex, value.z())
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Vector3f {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        var x = 0.0f
        var y = 0.0f
        var z = 0.0f
        var xExists = false
        var yExists = false
        var zExists = false
        if (dec.decodeSequentially()) {
            x = dec.decodeFloatElement(descriptor, XIndex)
            y = dec.decodeFloatElement(descriptor, YIndex)
            z = dec.decodeFloatElement(descriptor, ZIndex)
            xExists = true
            yExists = true
            zExists = true
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    XIndex -> {
                        x = dec.decodeFloatElement(descriptor, i)
                        xExists = true
                    }

                    YIndex -> {
                        y = dec.decodeFloatElement(descriptor, i)
                        yExists = true
                    }

                    ZIndex -> {
                        z = dec.decodeFloatElement(descriptor, i)
                        zExists = true
                    }

                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }


        dec.endStructure(descriptor)
        if (!xExists) x = missingField("x", "Vector3f") { 0.0f }
        if (!yExists) y = missingField("y", "Vector3f") { 0.0f }
        if (!zExists) z = missingField("z", "Vector3f") { 0.0f }

        return Vector3f(x, y, z)
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal open class PublicisedListLikeDescriptorImpl(elementDesc: SerialDescriptor, override val serialName: String) :
    PublicisedListLikeDescriptor(elementDesc)