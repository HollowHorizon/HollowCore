package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Vector4f
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.client.renderer.texture.TextureManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_CCW
import org.lwjgl.opengl.GL11.GL_CW
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.mixin.ShaderInstanceAccessor


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit,
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