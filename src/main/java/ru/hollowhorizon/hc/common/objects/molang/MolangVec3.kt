package ru.hollowhorizon.hc.common.objects.molang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.joml.Vector3f

@Serializable(with = MolangVec3Serializer::class)
data class MolangVec3(val x: MolangExpression, val y: MolangExpression, val z: MolangExpression) {
    fun eval(context: MolangContext): Vector3f =
        Vector3f(x.eval(context), y.eval(context), z.eval(context))

    companion object {
        val ZERO = MolangVec3(MolangExpression.ZERO, MolangExpression.ZERO, MolangExpression.ZERO)
        val UNIT_X = MolangVec3(MolangExpression.ONE, MolangExpression.ZERO, MolangExpression.ZERO)
        val UNIT_Y = MolangVec3(MolangExpression.ZERO, MolangExpression.ONE, MolangExpression.ZERO)
        val UNIT_Z = MolangVec3(MolangExpression.ZERO, MolangExpression.ZERO, MolangExpression.ONE)
    }
}

object MolangVec3Serializer : KSerializer<MolangVec3> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor
    override fun deserialize(decoder: Decoder): MolangVec3 = parse((decoder as JsonDecoder).decodeJsonElement())
    override fun serialize(encoder: Encoder, value: MolangVec3) = throw UnsupportedOperationException("Molang serialization not supported yet!")

    private fun parse(json: JsonElement): MolangVec3 = when (json) {
        is JsonArray -> {
            val first = (json[0] as JsonPrimitive).parseMolangExpression()
            val second = (json.getOrNull(1) as JsonPrimitive?)?.parseMolangExpression() ?: first
            val third = (json.getOrNull(2) as JsonPrimitive?)?.parseMolangExpression() ?: second
            MolangVec3(first, second, third)
        }
        is JsonPrimitive -> when (json.content) {
            "x" -> MolangVec3.UNIT_X
            "y" -> MolangVec3.UNIT_Y
            "z" -> MolangVec3.UNIT_Z
            else -> json.parseMolangExpression().let { MolangVec3(it, it, it) }
        }
        else -> throw SerializationException("Expected array or primitive, got $json")
    }
}