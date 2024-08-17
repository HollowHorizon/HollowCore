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

package ru.hollowhorizon.hc.client.render.shaders

import com.mojang.blaze3d.pipeline.RenderTarget

import net.minecraft.client.renderer.texture.AbstractTexture
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil

class Uniform(val name: String) {
    var id: Int = -1
}

fun Uniform.upload(first: Int) {
    GL20.glUniform1i(id, first)
}

fun Uniform.upload(first: AbstractTexture) {
    GL20.glUniform1i(id, first.id)
}

fun Uniform.upload(first: RenderTarget) {
    GL20.glUniform1i(id, first.colorTextureId)
}

fun Uniform.upload(first: Int, second: Int) {
    GL20.glUniform2i(id, first, second)
}

fun Uniform.upload(first: Int, second: Int, third: Int) {
    GL20.glUniform3i(id, first, second, third)
}

fun Uniform.upload(first: Int, second: Int, third: Int, fourth: Int) {
    GL20.glUniform4i(id, first, second, third, fourth)
}

fun Uniform.upload(first: Float) {
    GL20.glUniform1f(id, first)
}

fun Uniform.upload(first: Float, second: Float) {
    GL20.glUniform2f(id, first, second)
}

fun Uniform.upload(first: Float, second: Float, third: Float) {
    GL20.glUniform3f(id, first, second, third)
}

fun Uniform.upload(first: Float, second: Float, third: Float, fourth: Float) {
    GL20.glUniform4f(id, first, second, third, fourth)
}

private val matrixBuffer = MemoryUtil.memAllocFloat(16)

fun Uniform.upload(matrix: Matrix4f) {
    matrixBuffer.position(0)
    matrix.get(matrixBuffer)
    GL20.glUniformMatrix4fv(id, false, matrixBuffer)
}