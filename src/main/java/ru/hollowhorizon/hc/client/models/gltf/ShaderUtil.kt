package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit
) {
    pShaderInstance.apply()

    pShaderInstance.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    pShaderInstance.PROJECTION_MATRIX?.upload()

    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.set(RenderSystem.getInverseViewRotationMatrix())
    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.upload()

    pShaderInstance.FOG_START?.set(RenderSystem.getShaderFogStart())
    pShaderInstance.FOG_START?.upload()

    pShaderInstance.FOG_END?.set(RenderSystem.getShaderFogEnd())
    pShaderInstance.FOG_END?.upload()

    pShaderInstance.FOG_COLOR?.set(RenderSystem.getShaderFogColor())
    pShaderInstance.FOG_COLOR?.upload()

    pShaderInstance.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    pShaderInstance.FOG_SHAPE?.upload()

    pShaderInstance.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    pShaderInstance.COLOR_MODULATOR?.upload()

    GL33.glActiveTexture(GL33.GL_TEXTURE2) //Лайтмап
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, GltfManager.lightTexture.id)

    body()

    pShaderInstance.clear()
}