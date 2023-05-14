package ru.hollowhorizon.hc.client.ultralight.impl.renderer

import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.bitmap.UltralightBitmapSurface
import com.labymedia.ultralight.config.UltralightViewConfig
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12.*
import ru.hollowhorizon.hc.client.utils.mc
import java.nio.ByteBuffer

/**
 * A cpu renderer which is being supported on OpenGL functionality from version 1.2.
 */
class CpuViewRenderer : ViewRenderer {

    private var glTexture = -1

    override fun setupConfig(viewConfig: UltralightViewConfig) {
        // CPU rendering is not accelerated
        // viewConfig.isAccelerated(false)
    }

    override fun delete() {
        glDeleteTextures(glTexture)
        glTexture = -1
    }

    /**
     * Render the current view
     */
    override fun render(view: UltralightView, stack: MatrixStack) {
        if (glTexture == -1) {
            createGlTexture()
        }

        // As we are using the CPU renderer, draw with a bitmap (we did not set a custom surface)
        val surface = view.surface() as UltralightBitmapSurface
        val bitmap = surface.bitmap()

        val width = view.width().toInt()
        val height = view.height().toInt()

        // Prepare OpenGL for 2D textures and bind our texture
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        RenderSystem.bindTexture(glTexture)

        val dirtyBounds = surface.dirtyBounds()

        if (dirtyBounds.isValid) {
            val imageData = bitmap.lockPixels()

            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)
            glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0)
            glPixelStorei(GL_UNPACK_ROW_LENGTH, bitmap.rowBytes().toInt() / 4)

            if (dirtyBounds.width() == width && dirtyBounds.height() == height) {
                // Update full image
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    imageData
                )
            } else {
                // Update partial image
                val x = dirtyBounds.x()
                val y = dirtyBounds.y()
                val dirtyWidth = dirtyBounds.width()
                val dirtyHeight = dirtyBounds.height()
                val startOffset = (y * bitmap.rowBytes() + x * 4).toInt()

                glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    x, y, dirtyWidth, dirtyHeight,
                    GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                    imageData.position(startOffset) as ByteBuffer
                )
            }
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)

            bitmap.unlockPixels()
            surface.clearDirtyBounds()
        }

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableAlphaTest()
        RenderSystem.defaultAlphaFunc()

        RenderSystem.bindTexture(glTexture)

        AbstractGui.blit(stack, 0, 0, 0f, 0f, width, height, width, height)
    }

    /**
     * Sets up the OpenGL texture for rendering
     */
    private fun createGlTexture() {
        glTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, glTexture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

}
