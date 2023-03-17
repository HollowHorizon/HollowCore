package ru.hollowhorizon.hc.client.render.data

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.GLAllocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModel.MAX_WEIGHTS
import ru.hollowhorizon.hc.client.models.core.model.BakedAnimatedMesh


@OnlyIn(Dist.CLIENT)
class BTAnimatedMeshRenderData(private val animatedMesh: BakedAnimatedMesh) : BTMeshRenderData(
    animatedMesh
) {
    override fun uploadBuffers() {
        super.uploadBuffers()

        // weights
        var vboId: Int = genVBO()
        val weightsByteBuffer = GLAllocation.createByteBuffer(animatedMesh.weights.size * 4)
        val weightsBuffer = weightsByteBuffer.asFloatBuffer()
        weightsBuffer.put(animatedMesh.weights).flip()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) {vboId}
        RenderSystem.glBufferData(GL_ARRAY_BUFFER, weightsByteBuffer, GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(3)
        GL20.glVertexAttribPointer(3, MAX_WEIGHTS, GL_FLOAT, false, 0, 0)

        // bone ids
        vboId = genVBO()
        val boneIdsByteBuffer = GLAllocation.createByteBuffer(animatedMesh.boneIds.size * 4)
        val boneIdsBuffer = boneIdsByteBuffer.asIntBuffer()
        boneIdsBuffer.put(animatedMesh.boneIds).flip()
        RenderSystem.glBindBuffer(GL_ARRAY_BUFFER) {vboId}
        RenderSystem.glBufferData(GL_ARRAY_BUFFER, boneIdsByteBuffer, GL_STATIC_DRAW)
        GL30.glEnableVertexAttribArray(4)
        GL20.glVertexAttribPointer(4, MAX_WEIGHTS, GL_FLOAT, false, 0, 0)
    }
}