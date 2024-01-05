package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Matrix4f
import com.mojang.math.Vector4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit
) {
    RenderSystem.setupShaderLights(pShaderInstance)

    pShaderInstance.apply()

    pShaderInstance.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    pShaderInstance.PROJECTION_MATRIX?.upload()

    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.set(RenderSystem.getInverseViewRotationMatrix())
    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.upload()

    pShaderInstance.FOG_START?.set(RenderSystem.getShaderFogStart())
    pShaderInstance.FOG_START?.upload()

    pShaderInstance.FOG_END?.set(RenderSystem.getShaderFogEnd())
    pShaderInstance.FOG_END?.upload()

    pShaderInstance.FOG_COLOR?.set(Vector4f(1.0f, 1.0f, 1.0f, 1.0f))
    pShaderInstance.FOG_COLOR?.upload()

    pShaderInstance.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    pShaderInstance.FOG_SHAPE?.upload()

    pShaderInstance.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    pShaderInstance.COLOR_MODULATOR?.upload()

    GL33.glUniform1i(GL33.glGetUniformLocation(pShaderInstance.id, "Sampler0"), 0)
    GL33.glUniform1i(GL33.glGetUniformLocation(pShaderInstance.id, "Sampler1"), 1)
    GL33.glUniform1i(GL33.glGetUniformLocation(pShaderInstance.id, "Sampler2"), 2)

    body()

    pShaderInstance.clear()
}