package ru.hollowhorizon.hc.client.render.data

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.GLAllocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.models.core.model.BakedMesh


@OnlyIn(Dist.CLIENT)
open class BTMeshRenderData(private val mesh: BakedMesh) : IBTRenderData {
    private var vaoId: Int
    private val vboIdList: MutableList<Int>
    private val vertexCount: Int

    init {
        vertexCount = mesh.indices.size
        vboIdList = ArrayList()
        vaoId = -1
    }

    open fun uploadBuffers() {
        var vboId = genVBO()

        // Position Data
        val posByteBuffer = GLAllocation.createByteBuffer(mesh.positions.size * 4)
        val posBuffer = posByteBuffer.asFloatBuffer()
        posBuffer.put(mesh.positions).flip()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) { vboId }
        RenderSystem.glBufferData(GL_ARRAY_BUFFER, posByteBuffer, GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        // UV Data
        vboId = genVBO()
        val textCoordsByteBuffer = GLAllocation.createByteBuffer(mesh.texCoords.size * 4)
        val textCoordsBuffer = textCoordsByteBuffer.asFloatBuffer()
        textCoordsBuffer.put(mesh.texCoords).flip()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) { vboId }
        RenderSystem.glBufferData(GL_ARRAY_BUFFER, textCoordsByteBuffer, GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

        // Normal Data
        vboId = genVBO()
        val vecNormalsByteBuffer = GLAllocation.createByteBuffer(mesh.normals.size * 4)
        val vecNormalsBuffer = vecNormalsByteBuffer.asFloatBuffer()
        vecNormalsBuffer.put(mesh.normals).flip()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) { vboId }
        RenderSystem.glBufferData(GL_ARRAY_BUFFER, vecNormalsByteBuffer, GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0)


        // Indices Data
        vboId = genVBO()
        val indicesByteBuffer = GLAllocation.createByteBuffer(mesh.indices.size * 4)
        val indicesBuffer = indicesByteBuffer.asIntBuffer()
        indicesBuffer.put(mesh.indices).flip()
        RenderSystem.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER) { vboId }
        RenderSystem.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesByteBuffer, GL_STATIC_DRAW)
    }

    protected fun genVBO(): Int {
        var vbo = 0
        RenderSystem.glGenBuffers {
            vboIdList.add(vbo)
            vbo = it
        }
        if (vbo == 0) throw RuntimeException("Could not create VBO, not render thread?")
        return vbo
    }

    override fun render() {
        initRender()
        GL11.glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)
        endRender()
    }

    fun initRender() {
        // Draw the mesh
        GL30.glBindVertexArray(vaoId)
    }

    fun endRender() {
        GL30.glBindVertexArray(0)
    }

    override fun cleanup() {
        GL30.glDisableVertexAttribArray(0)

        // Delete the VBOs
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) { 0 }
        for (vboId in vboIdList) {
            RenderSystem.glDeleteBuffers(vboId)
        }

        // Delete the VAO
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId);
    }

    override fun upload() {
        vaoId = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vaoId)
        uploadBuffers()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) { 0 }
        GL30.glBindVertexArray(0)
    }
}