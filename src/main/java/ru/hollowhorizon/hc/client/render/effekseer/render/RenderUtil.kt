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

package ru.hollowhorizon.hc.client.render.effekseer.render

import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

object RenderUtil {

    @JvmStatic
    fun copyCurrentDepthTo(target: RenderTarget) {
        val frameBuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)
        val window = MC.window
        copyDepthSafely(frameBuffer, window.width, window.height, target)
    }

    @JvmStatic
    fun pasteToCurrentDepthFrom(source: RenderTarget) {
        val frameBuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)
        val window = MC.window
        copyDepthSafely(source, frameBuffer, window.width, window.height)
    }


    fun copyDepthSafely(src: Int, srcWidth: Int, srcHeight: Int, target: RenderTarget) {
        val readBackup = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING)
        val drawBackup = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.frameBufferId)
        GL30.glBlitFramebuffer(0, 0, srcWidth, srcHeight, 0, 0, target.width, target.height, 256, 9728)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readBackup)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawBackup)
    }

    fun copyDepthSafely(src: RenderTarget, target: Int, targetWidth: Int, targetHeight: Int) {
        val readBackup = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING)
        val drawBackup = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.frameBufferId)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target)
        GL30.glBlitFramebuffer(0, 0, src.width, src.height, 0, 0, targetWidth, targetHeight, 256, 9728)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readBackup)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawBackup)
    }

    private val MC: Minecraft = Minecraft.getInstance()
}
