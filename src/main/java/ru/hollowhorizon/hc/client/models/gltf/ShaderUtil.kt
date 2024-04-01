package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.mixins.ShaderInstanceAccessor


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit,
) {
    //TODO: Не стоит использовать стандарные apply и clear, они бонусом тебе ещё кучу ненужных фич включают

    pShaderInstance.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    pShaderInstance.MODEL_VIEW_MATRIX?.set(RenderSystem.getModelViewMatrix())
    pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX?.set(RenderSystem.getInverseViewRotationMatrix())
    pShaderInstance.FOG_START?.set(RenderSystem.getShaderFogStart())
    pShaderInstance.FOG_END?.set(RenderSystem.getShaderFogEnd())
    pShaderInstance.FOG_COLOR?.set(RenderSystem.getShaderFogColor())
    pShaderInstance.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    pShaderInstance.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    pShaderInstance.TEXTURE_MATRIX?.set(RenderSystem.getTextureMatrix().copy().apply { transpose() })
    pShaderInstance.GAME_TIME?.set(RenderSystem.getShaderGameTime())

    RenderSystem.setupShaderLights(pShaderInstance)

    pShaderInstance.apply()
    
    RenderSystem.enableDepthTest()
    RenderSystem.enableBlend()
    RenderSystem.depthFunc(515)
    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)


    val accessor = pShaderInstance as ShaderInstanceAccessor
    accessor.samplerLocations().forEachIndexed { texture, index ->
        GL33.glUniform1i(index, texture)
    }

    body()

    RenderSystem.depthFunc(515)

    pShaderInstance.clear()
}

val ENTITY_SHADER get() = GameRenderer.getRendertypeEntityCutoutShader()!!

const val COLOR_MAP_INDEX = GL13.GL_TEXTURE0
const val NORMAL_MAP_INDEX = GL13.GL_TEXTURE1
const val SPECULAR_MAP_INDEX = GL13.GL_TEXTURE3