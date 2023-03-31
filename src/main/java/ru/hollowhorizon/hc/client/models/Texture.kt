package ru.hollowhorizon.hc.client.models

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil
import ru.hollowhorizon.hc.HollowCore
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Texture(imageBuffer: ByteBuffer?) {
    var id = 0

    var width = 0

    var height = 0

    var numRows = 1

    var numCols = 1

    init {
        imageBuffer?.let { imageData ->
            stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val avChannels = stack.mallocInt(1)

                // Decode texture image into a byte buffer
                val decodedImage: ByteBuffer = stbi_load_from_memory(imageData, w, h, avChannels, 4)!!
                width = w.get()
                height = h.get()

                // Create a new OpenGL texture
                id = glGenTextures()
                // Bind the texture
                glBindTexture(GL_TEXTURE_2D, id)

                // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                // Upload the texture data
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, decodedImage)
                // Generate Mip Map
                glGenerateMipmap(GL_TEXTURE_2D)
                stbi_image_free(decodedImage)
            }
        }
    }

    @Throws(Exception::class)
    constructor(width: Int, height: Int, pixelFormat: Int) : this(null) {
        id = glGenTextures()
        this.width = width
        this.height = height
        glBindTexture(GL_TEXTURE_2D, id)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_DEPTH_COMPONENT,
            this.width,
            this.height,
            0,
            pixelFormat,
            GL_FLOAT,
            null as ByteBuffer?
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    }

    @Throws(Exception::class)
    constructor(fileName: String, numCols: Int, numRows: Int): this(fileName) {
        this.numCols = numCols
        this.numRows = numRows
    }

    @Throws(Exception::class)
    constructor(fileName: String): this(ioResourceToByteBuffer(fileName, 1024))

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun cleanup() {
        glDeleteTextures(id)
    }

    companion object {
        @Throws(IOException::class)
        fun ioResourceToByteBuffer(resource: String, bufferSize: Int): ByteBuffer {
            var buffer: ByteBuffer
            val path: Path = Paths.get(resource)
            if (Files.isReadable(path)) {
                Files.newByteChannel(path).use { fc ->
                    buffer = MemoryUtil.memAlloc(fc.size().toInt() + 1)
                    while (fc.read(buffer) !== -1);
                }
            } else {
                HollowCore::class.java.getResourceAsStream(resource).use { source ->
                    Channels.newChannel(source!!).use { rbc ->
                        buffer = MemoryUtil.memAlloc(bufferSize)
                        while (true) {
                            val bytes: Int = rbc.read(buffer)
                            if (bytes == -1) {
                                break
                            }
                            if (buffer.remaining() == 0) {
                                buffer = resizeBuffer(buffer, buffer.capacity() * 2)
                            }
                        }
                    }
                }
            }
            buffer.flip()
            return buffer
        }

        private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
            val newBuffer = BufferUtils.createByteBuffer(newCapacity)
            buffer.flip()
            newBuffer.put(buffer)
            return newBuffer
        }
    }
}