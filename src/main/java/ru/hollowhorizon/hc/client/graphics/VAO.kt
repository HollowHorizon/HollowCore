package ru.hollowhorizon.hc.client.graphics

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

@OnlyIn(Dist.CLIENT)
class VAO(private val id: Int) {
    private val dataVBOs: MutableCollection<VBO> = ArrayList()
    private var indexVbo: VBO? = null
    private val attributesSet = HashSet<Int>()
    var indexCount = 0
        private set

    fun createIndexBuffer(indices: IntArray) {
        indexVbo = GPUMemoryManager.instance.createVBO(GL15.GL_ELEMENT_ARRAY_BUFFER)
        indexVbo?.bind()
        indexVbo?.storeData(indices)
        indexCount = indices.size
    }

    fun createAttribute(attribute: Int, data: FloatArray, attrSize: Int) {
        val dataVbo = GPUMemoryManager.instance.createVBO(GL15.GL_ARRAY_BUFFER)
        dataVbo.bind()
        dataVbo.storeData(data)
        GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0)
        dataVbo.unbind()
        dataVBOs.add(dataVbo)
        attributesSet.add(attribute)
    }

    fun createIntAttribute(attribute: Int, data: IntArray?, attrSize: Int) {
        val dataVbo: VBO = GPUMemoryManager.instance.createVBO(GL15.GL_ARRAY_BUFFER)
        dataVbo.bind()
        dataVbo.storeData(data!!)
        GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0)
        dataVbo.unbind()
        dataVBOs.add(dataVbo)
        attributesSet.add(attribute)
    }

    @JvmOverloads
    fun bind(vararg attributes: Int = attributesSet.toIntArray()) {
        bindVAO()
        for (i in attributes) {
            GL20.glEnableVertexAttribArray(i)
        }
    }

    @JvmOverloads
    fun unbind(vararg attributes: Int = attributesSet.toIntArray()) {
        for (i in attributes) {
            GL20.glDisableVertexAttribArray(i)
        }
        unbindVAO()
    }

    fun bindVAO() {
        GL30.glBindVertexArray(id)
    }

    fun unbindVAO() {
        GL30.glBindVertexArray(0)
    }

    companion object {
        private const val BYTES_PER_FLOAT = 4
        private const val BYTES_PER_INT = 4
    }
}