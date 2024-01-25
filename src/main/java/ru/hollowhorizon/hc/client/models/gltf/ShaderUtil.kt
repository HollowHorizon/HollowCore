package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Matrix4f
import com.mojang.math.Vector4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.mixin.ShaderInstanceAccessor


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit
) {
    RenderSystem.setupShaderLights(pShaderInstance)
    //TODO: Не стоит использовать стандарные apply и clear, они бонусом тебе ещё кучу ненужных фич включают
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

    GL33.glEnable(GL33.GL_DEPTH_TEST)
    GL33.glEnable(GL33.GL_DEPTH)

    val accessor = pShaderInstance as ShaderInstanceAccessor
    accessor.samplerLocations().forEachIndexed { texture, index ->
        GL33.glUniform1i(index, texture)
    }

    body()

    pShaderInstance.clear()
}