package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.widget.Widget.blit
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import ru.hollowhorizon.hc.client.utils.math.VertexExt
import ru.hollowhorizon.hc.client.utils.math.plusAssign
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.toRGBA
import ru.hollowhorizon.hc.client.utils.use

object DrawUtils {
    fun drawBox(stack: MatrixStack, location: ResourceLocation, x: Int, y: Int, width: Int, height: Int, color: Int) {
        val rgba = color.toRGBA()

        stack.pushPose()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableAlphaTest()

        RenderSystem.color4f(rgba.r, rgba.g, rgba.b, rgba.a)

        mc.textureManager.bind(location)

        val size = if (width > height) width / 3 else height / 3

        blit(stack, x, y, 0F, 0F, size, size, size * 3, size * 3)
        blit(stack, x + size, y, width - size * 2F, 0F, width - size * 2, size, (width - size * 2) * 3, size * 3)
        blit(stack, x + width - size, y, size * 2F, 0F, size, size, size * 3, size * 3)

        if(width > height) {
            blit(stack, x, y + size, 0F, size * 1F, size, height - size * 2, size * 3, (height - size * 2) * 3)
            blit(stack, x + size, y + size, width - size * 2F, size * 1F, width - size * 2, height - size * 2, (width - size * 2) * 3, (height - size * 2) * 3)
            blit(stack,x + width - size, y + size, size * 2F, size * 1F, size, height - size * 2, size * 3, (height - size * 2) * 3)
        } else {
            blit(stack, x, y + size, 0F, height - size * 2F, size, height - size * 2, size * 3, (height - size * 2) * 3)
            blit(stack, x + size, y + size, width - size * 2F, height - size * 2F, width - size * 2, height - size * 2, (width - size * 2) * 3, (height - size * 2) * 3)
            blit(stack, x + width - size, y + size, size * 2F, height - size * 2F, size, height - size * 2, size * 3, (height - size * 2) * 3)
        }

        blit(stack, x, y + height - size, 0F, size * 2F, size, size, size * 3, size * 3)
        blit(stack, x + size, y + height - size, width - size * 2F, size * 2F, width - size * 2, size, (width - size * 2) * 3, size * 3)
        blit(stack, x + width - size, y + height - size, size * 2F, size * 2F, size, size, size * 3, size * 3)

        stack.popPose()
    }

    fun drawBounds(stack: MatrixStack, x1: Int, y1: Int, x2: Int, y2: Int, width: Int, color: Int) {
        val rgba = color.toRGBA()

        stack.use {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableAlphaTest()

            GL11.glLineWidth(width.toFloat())
            val vb = Tessellator.getInstance().builder
            vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)

            vb += VertexExt(stack, x1, y1, 0, rgba)
            vb += VertexExt(stack, x1, y2, 0, rgba)

            vb += VertexExt(stack, x1, y2, 0, rgba)
            vb += VertexExt(stack, x2, y2, 0, rgba)

            vb += VertexExt(stack, x2, y2, 0, rgba)
            vb += VertexExt(stack, x2, y1, 0, rgba)

            vb += VertexExt(stack, x2, y1, 0, rgba)
            vb += VertexExt(stack, x1, y1, 0, rgba)

            Tessellator.getInstance().end()

            GL11.glLineWidth(1f)
        }
    }

}