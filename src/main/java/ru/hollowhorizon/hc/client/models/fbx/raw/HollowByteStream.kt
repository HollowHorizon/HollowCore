package ru.hollowhorizon.hc.client.models.fbx.raw

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.DataFormatException
import java.util.zip.Inflater

class HollowByteStream {
    private val bytes: ByteArrayInputStream
    private val size: Int

    constructor(stream: InputStream) {
        bytes = ByteArrayInputStream(stream.readBytes())
        size = bytes.available()
    }

    constructor(bytes: ByteArray) {
        this.bytes = ByteArrayInputStream(bytes)
        size = bytes.size
    }

    @Throws(IOException::class)
    fun readString(): String {
        val len = readByte()
        return String(read(len.toInt()))
    }

    @Throws(IOException::class)
    fun readBigString(): String {
        val len = readUInt()
        return String(read(len))
    }

    @Throws(IOException::class)
    fun readUInt(): Int {
        return ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()
    }

    @Throws(IOException::class)
    fun readByte(): Byte {
        return read(1)[0]
    }

    @Throws(IOException::class)
    fun readChar(): Char {
        return Char(read(1)[0].toUShort())
    }

    @Throws(IOException::class)
    fun read(length: Int): ByteArray {
        val data = ByteArray(length)
        bytes.read(data)
        return data
    }

    @Throws(IOException::class)
    fun readInt(): Int {
        return ByteBuffer.wrap(read(2)).getInt()
    }

    @Throws(IOException::class)
    fun readFloat(): Float {
        return ByteBuffer.wrap(read(4)).getFloat()
    }

    @Throws(IOException::class)
    fun readDouble(): Double {
        return ByteBuffer.wrap(read(8)).getDouble()
    }

    @Throws(IOException::class)
    fun readLong(): Long {
        return ByteBuffer.wrap(read(8)).getLong()
    }

    @Throws(IOException::class)
    fun readIntArray(): IntArray {
        val buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        val array = IntArray(buffer.remaining())
        buffer[array]
        return array
    }

    @Throws(IOException::class)
    fun readLongArray(): LongArray {
        val buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer()
        val array = LongArray(buffer.remaining())
        buffer[array]
        return array
    }

    @Throws(IOException::class)
    fun readBoolArray(): BooleanArray {
        val binaryData = readRawArray(1)
        val result = BooleanArray(binaryData.size)
        for (i in binaryData.indices) {
            result[i] = binaryData[i].toInt() != 0
        }
        return result
    }

    @Throws(IOException::class)
    fun readFloatArray(): FloatArray {
        val buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        val array = FloatArray(buffer.remaining())
        buffer[array]
        return array
    }

    @Throws(IOException::class)
    fun readDoubleArray(): DoubleArray {
        val buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer()
        val array = DoubleArray(buffer.remaining())
        buffer[array]
        return array
    }

    @Throws(IOException::class)
    fun readRawArray(dataSize: Int): ByteArray {
        val length = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()
        val encoding = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()
        val compressedSize = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()
        return if (encoding == 1) decompressData(read(compressedSize))
        else read(length * dataSize)
    }

    private fun decompressData(compressedData: ByteArray): ByteArray {
        val decompressor = Inflater()
        decompressor.setInput(compressedData)
        val outputStream = ByteArrayOutputStream(compressedData.size)
        val buffer = ByteArray(1024)
        while (!decompressor.finished()) {
            try {
                val count = decompressor.inflate(buffer)
                outputStream.write(buffer, 0, count)
            } catch (e: DataFormatException) {
                e.printStackTrace()
            }
        }
        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        decompressor.end()
        return outputStream.toByteArray()
    }

    fun available(): Int {
        return size - bytes.available()
    }

    fun size(): Int {
        return size
    }
}
