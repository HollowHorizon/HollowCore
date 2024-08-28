package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

class ClientVec3Argument : ArgumentType<Vec3> {
    companion object {
        fun vec3() = ClientVec3Argument()
        fun getVec3(context: CommandContext<SharedSuggestionProvider>, arg: String): Vec3 {
            return context.getArgument(arg, Vec3::class.java)
        }
    }

    override fun parse(reader: StringReader): Vec3 {
        val i = reader.cursor
        val worldCoordinate = WorldCoordinate.parseDouble(reader, false)
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip()
            val worldCoordinate2 = WorldCoordinate.parseDouble(reader, false)
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip()
                val worldCoordinate3 = WorldCoordinate.parseDouble(reader, false)
                return Vec3(worldCoordinate.toReal(0), worldCoordinate2.toReal(1), worldCoordinate3.toReal(3))
            } else {
                reader.cursor = i
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader)
            }
        } else {
            reader.cursor = i
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader)
        }
    }

    class WorldCoordinate(val relative: Boolean, val value: Double) {
        companion object {
            val ERROR_EXPECTED_DOUBLE: SimpleCommandExceptionType =
                SimpleCommandExceptionType(Component.translatable("argument.pos.missing.double"))

            fun parseDouble(
                reader: StringReader,
                centerCorrect: Boolean,
            ): WorldCoordinate {
                if (reader.canRead() && reader.peek() == '^') {
                    throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(reader)
                } else if (!reader.canRead()) {
                    throw ERROR_EXPECTED_DOUBLE.createWithContext(reader)
                } else {
                    val bl = isRelative(reader)
                    val i = reader.cursor
                    var d = if (reader.canRead() && reader.peek() != ' ') reader.readDouble() else 0.0
                    val string = reader.string.substring(i, reader.cursor)
                    if (bl && string.isEmpty()) {
                        return WorldCoordinate(true, 0.0)
                    } else {
                        if (!string.contains(".") && !bl && centerCorrect) d += 0.5
                        return WorldCoordinate(bl, d)
                    }
                }
            }

            fun isRelative(reader: StringReader): Boolean {
                val bl: Boolean
                if (reader.peek() == '~') {
                    bl = true
                    reader.skip()
                } else bl = false

                return bl
            }
        }

        fun toReal(id: Int) = if (relative) local(id) + value else value

        private fun local(id: Int) = when (id) {
            0 -> Minecraft.getInstance().player?.x ?: 0.0
            1 -> Minecraft.getInstance().player?.y ?: 0.0
            2 -> Minecraft.getInstance().player?.z ?: 0.0
            else -> throw IllegalStateException("ID $id does not exist")
        }
    }
}