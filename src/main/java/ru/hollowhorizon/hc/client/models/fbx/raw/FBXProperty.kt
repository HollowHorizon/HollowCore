/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
