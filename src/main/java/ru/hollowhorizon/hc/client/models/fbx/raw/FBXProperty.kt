package ru.hollowhorizon.hc.client.models.fbx.raw

import java.io.IOException

class FBXProperty<T>(val character: Char, private val data: T) {
    override fun toString(): String {
        return "<$character, $data>"
    }

    @Suppress("UNCHECKED_CAST")
    fun <K> getData(): K {
        return data as K
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun load(stream: HollowByteStream, character: Char): FBXProperty<*> {
            return when (character) {
                'Y' -> FBXProperty(character, stream.readInt())
                'C' -> FBXProperty(character, stream.readByte().toInt() == 1)
                'I' -> FBXProperty(character, stream.readUInt())
                'F' -> FBXProperty(character, stream.readFloat())
                'D' -> FBXProperty(character, stream.readDouble())
                'L' -> FBXProperty(character, stream.readLong())
                'S', 'R' -> FBXProperty(character, stream.readBigString())
                'f' -> FBXProperty(character, stream.readFloatArray())
                'i' -> FBXProperty(character, stream.readIntArray())
                'd' -> FBXProperty(character, stream.readDoubleArray())
                'l' -> FBXProperty(character, stream.readLongArray())
                'b' -> FBXProperty(character, stream.readBoolArray())
                'c' -> FBXProperty(character, stream.readRawArray(1))
                else -> FBXProperty<Any?>(character, null)
            }
        }
    }
}
