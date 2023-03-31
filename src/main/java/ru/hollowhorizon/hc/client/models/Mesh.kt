package ru.hollowhorizon.hc.client.models

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.abs


open class Mesh(
    positions: FloatArray,
    textCoords: FloatArray,
    normals: FloatArray,
    indices: IntArray,
    jointIndices: IntArray,
    weights: FloatArray,
) {
    companion object {
        const val MAX_WEIGHTS = 4

        protected fun createEmptyFloatArray(length: Int, defaultValue: Float): FloatArray {
            val result = FloatArray(length)
            Arrays.fill(result, defaultValue)
            return result
        }

        protected fun createEmptyIntArray(length: Int, defaultValue: Int): IntArray {
            val result = IntArray(length)
            Arrays.fill(result, defaultValue)
            return result
        }
    }

    var vaoId = 0

    var vboIdList: MutableList<Int>

    var vertexCount = 0

    var material: Material? = null

    var boundingRadius = 0f

    constructor(positions: FloatArray, textCoords: FloatArray, normals: FloatArray, indices: IntArray) :
            this(
                positions, textCoords, normals, indices,
                createEmptyIntArray(MAX_WEIGHTS * positions.size / 3, 0),
                createEmptyFloatArray(MAX_WEIGHTS * positions.size / 3, 0f)
            )


    init {
        val posBuffer = MemoryUtil.memAllocFloat(positions.size)
        var textCoordsBuffer: FloatBuffer? = null
        var vecNormalsBuffer: FloatBuffer? = null
        val weightsBuffer = MemoryUtil.memAllocFloat(weights.size)
        val jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.size)
        val indicesBuffer = MemoryUtil.memAllocInt(indices.size)
        try {
            calculateBoundingRadius(positions)
            vertexCount = indices.size
            vboIdList = ArrayList()
            vaoId = glGenVertexArrays()
            glBindVertexArray(vaoId)

            // Position VBO
            var vboId: Int =
                glGenBuffers()
            vboIdList.add(vboId)
            posBuffer.put(positions).flip()

            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

            // Texture coordinates VBO
            vboId = glGenBuffers()
            vboIdList.add(vboId)
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.size)
            if (textCoordsBuffer.capacity() > 0) {
                textCoordsBuffer.put(textCoords).flip()
            } else {
                // Create empty structure. Two coordinates for each 3 position coordinates
                textCoordsBuffer = MemoryUtil.memAllocFloat(positions.size * 3 / 2)
            }
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer!!, GL_STATIC_DRAW)
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

            // Vertex normals VBO
            vboId = glGenBuffers()
            vboIdList.add(vboId)
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.size)
            if (vecNormalsBuffer.capacity() > 0) {
                vecNormalsBuffer.put(normals).flip()
            } else {
                vecNormalsBuffer = MemoryUtil.memAllocFloat(positions.size)
            }
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer!!, GL_STATIC_DRAW)
            glEnableVertexAttribArray(2)
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0)

            // Weights
            vboId = glGenBuffers()
            vboIdList.add(vboId)
            weightsBuffer.put(weights).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(3)
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0)

            // Joint indices
            vboId = glGenBuffers()
            vboIdList.add(vboId)
            jointIndicesBuffer.put(jointIndices).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(4)
            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0)

            // Index VBO
            vboId = glGenBuffers()
            vboIdList.add(vboId)
            indicesBuffer.put(indices).flip()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)
        } finally {
            posBuffer.let { MemoryUtil.memFree(it) }
            textCoordsBuffer?.let { MemoryUtil.memFree(it) }
            vecNormalsBuffer?.let { MemoryUtil.memFree(it) }
            weightsBuffer.let { MemoryUtil.memFree(it) }
            jointIndicesBuffer.let { MemoryUtil.memFree(it) }
            indicesBuffer.let { MemoryUtil.memFree(it) }
        }
    }

    private fun calculateBoundingRadius(positions: FloatArray) {
        val length = positions.size
        boundingRadius = 0f
        for (i in 0 until length) {
            val pos = positions[i]
            boundingRadius = abs(pos).coerceAtLeast(boundingRadius)
        }
    }

    protected fun initRender() {
        val texture = material?.texture
        if (texture != null) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE0)
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.id)
        }
        val normalMap = material?.normalMap
        if (normalMap != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE1)
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, normalMap.id)
        }

        // Draw the mesh
        glBindVertexArray(vaoId)
    }

    protected fun endRender() {
        // Restore state
        glBindVertexArray(0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun render() {
        initRender()
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)
        endRender()
    }

    fun cleanUp() {
        glDisableVertexAttribArray(0)

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            glDeleteBuffers(vboId)
        }

        // Delete the texture
        material?.texture?.cleanup()

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }

    fun deleteBuffers() {
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            glDeleteBuffers(vboId)
        }

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }


}