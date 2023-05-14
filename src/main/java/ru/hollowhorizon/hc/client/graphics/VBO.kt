package ru.hollowhorizon.hc.client.graphics

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15
import java.nio.FloatBuffer
import java.nio.IntBuffer


class VBO(private val vboId: Int, private val type: Int) {

    fun bind() {
        GL15.glBindBuffer(type, vboId)
    }

    fun unbind() {
        GL15.glBindBuffer(type, 0)
    }

    fun use(action: VBO.() -> Unit) {
        bind()
        action()
        unbind()
    }

    fun storeData(data: FloatArray) {
        val buffer = BufferUtils.createFloatBuffer(data.size)
        buffer.put(data)
        buffer.flip()
        storeData(buffer)
    }

    fun storeData(data: IntArray) {
        val buffer = BufferUtils.createIntBuffer(data.size)
        buffer.put(data)
        buffer.flip()
        storeData(buffer)
    }

    fun storeData(data: IntBuffer) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW)
    }

    fun storeData(data: FloatBuffer) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW)
    }
}