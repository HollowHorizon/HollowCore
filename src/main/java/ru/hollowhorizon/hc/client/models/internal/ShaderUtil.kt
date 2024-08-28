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

package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.client.renderer.texture.TextureManager
import org.joml.Matrix4f
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.mixins.ShaderInstanceAccessor


inline fun drawWithShader(
    shader: ShaderInstance,
    body: () -> Unit,
) {
    val state = RenderType.entityCutout(TextureManager.INTENTIONAL_MISSING_TEXTURE)
    val accessor = shader as ShaderInstanceAccessor

    state.setupRenderState()
    //? if <1.21 {
    shader.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    shader.MODEL_VIEW_MATRIX?.set(RenderSystem.getModelViewMatrix())
    shader.INVERSE_VIEW_ROTATION_MATRIX?.set(RenderSystem.getInverseViewRotationMatrix())
    shader.FOG_START?.set(RenderSystem.getShaderFogStart())
    shader.FOG_END?.set(RenderSystem.getShaderFogEnd())
    shader.FOG_COLOR?.set(RenderSystem.getShaderFogColor())
    shader.FOG_SHAPE?.set(RenderSystem.getShaderFogShape().index)
    shader.COLOR_MODULATOR?.set(1.0F, 1.0F, 1.0F, 1.0F)
    shader.GAME_TIME?.set(RenderSystem.getShaderGameTime())
    RenderSystem.setupShaderLights(shader)
    //?} else {
    /*shader.setDefaultUniforms(
        VertexFormat.Mode.TRIANGLES,
        RenderSystem.getModelViewMatrix(),
        RenderSystem.getProjectionMatrix(),
        Minecraft.getInstance().window
    )
    *///?}

    //? if >=1.20.1 {
    /*shader.TEXTURE_MATRIX?.set(Matrix4f(RenderSystem.getTextureMatrix()).apply { transpose() })
    *///?} else {
    shader.TEXTURE_MATRIX?.set(RenderSystem.getTextureMatrix().copy().apply { transpose() })
    //?}
    shader.apply()

    accessor.samplerLocations().forEachIndexed { texture, index ->
        GL33.glUniform1i(index, texture)
    }

    body()

    shader.clear()
    state.clearRenderState()
}

val ENTITY_SHADER get() = GameRenderer.getRendertypeEntityCutoutShader()!!

const val COLOR_MAP_INDEX = GL13.GL_TEXTURE0
const val NORMAL_MAP_INDEX = GL13.GL_TEXTURE1
const val SPECULAR_MAP_INDEX = GL13.GL_TEXTURE3