/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ShaderInstance
import org.joml.Matrix4f
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.mixins.ShaderInstanceAccessor


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit,
) {
    pShaderInstance.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    pShaderInstance.MODEL_VIEW_MATRIX?.set(RenderSystem.getModelViewMatrix())
    pShaderInstance.FOG_START?.set(RenderSystem.getShaderFogStart())
    pShaderInstance.FOG_END?.set(RenderSystem.getShaderFogEnd())
    pShaderInstance.FOG_COLOR?.set(RenderSystem.getShaderFogColor())
    pShaderInstance.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    pShaderInstance.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    pShaderInstance.TEXTURE_MATRIX?.set(Matrix4f(RenderSystem.getTextureMatrix()).transpose())
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