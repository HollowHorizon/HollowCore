package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Matrix3f
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.GL33

fun drawWithShader(pShaderInstance: ShaderInstance, pModelViewMatrix: Matrix4f, pProjectionMatrix: Matrix4f, normal: Matrix3f, count: Int) {
    for (i in 0..11) {
        val j = RenderSystem.getShaderTexture(i)
        pShaderInstance.setSampler("Sampler$i", j)
    }
    pShaderInstance.MODEL_VIEW_MATRIX?.set(pModelViewMatrix)
    pShaderInstance.PROJECTION_MATRIX?.set(pProjectionMatrix)
    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.set(RenderSystem.getInverseViewRotationMatrix())
    pShaderInstance.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    pShaderInstance.FOG_START?.set(RenderSystem.getShaderFogStart())
    pShaderInstance.FOG_END?.set(RenderSystem.getShaderFogEnd())
    pShaderInstance.FOG_COLOR?.set(RenderSystem.getShaderFogColor())
    pShaderInstance.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    pShaderInstance.TEXTURE_MATRIX?.set(RenderSystem.getTextureMatrix())
    pShaderInstance.GAME_TIME?.set(RenderSystem.getShaderGameTime())
    val window = Minecraft.getInstance().window
    pShaderInstance.SCREEN_SIZE?.set(window.width.toFloat(), window.height.toFloat())

    RenderSystem.setupShaderLights(pShaderInstance)

    val light0Buffer = pShaderInstance.LIGHT0_DIRECTION!!.floatBuffer
    val light0 = Vector3f(light0Buffer.get(0), light0Buffer.get(1), light0Buffer.get(2))
    val light1Buffer = pShaderInstance.LIGHT1_DIRECTION!!.floatBuffer
    val light1 = Vector3f(light1Buffer.get(0), light1Buffer.get(1), light1Buffer.get(2))

    light0.transform(normal)
    light1.transform(normal)

    pShaderInstance.LIGHT0_DIRECTION?.set(light0)
    pShaderInstance.LIGHT1_DIRECTION?.set(light1)

    pShaderInstance.apply()
    RenderSystem.drawElements(GL33.GL_TRIANGLES, count, GL33.GL_UNSIGNED_INT)
    pShaderInstance.clear()
}