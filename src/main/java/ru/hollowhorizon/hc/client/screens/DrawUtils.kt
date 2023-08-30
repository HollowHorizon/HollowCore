package ru.hollowhorizon.hc.client.screens


import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiComponent.blit
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.GL11
import ru.hollowhorizon.hc.client.utils.math.VertexExt
import ru.hollowhorizon.hc.client.utils.math.plusAssign
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.toRGBA
import ru.hollowhorizon.hc.client.utils.use

object DrawUtils {
    @Suppress("DEPRECATION")
    fun drawBox(stack: PoseStack, location: ResourceLocation, x: Int, y: Int, width: Int, height: Int, color: Int) {
        val rgba = color.toRGBA()

        stack.pushPose()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        GL11.glColor4f(rgba.r, rgba.g, rgba.b, rgba.a)

        RenderSystem.setShaderTexture(0, location)

        val size = if (width > height) width / 3 else height / 3

        blit(stack, x, y, 0F, 0F, size, size, size * 3, size * 3)
        blit(stack, x + size, y, width - size * 2F, 0F, width - size * 2, size, (width - size * 2) * 3, size * 3)
        blit(stack, x + width - size, y, size * 2F, 0F, size, size, size * 3, size * 3)

        if (width > height) {
            blit(stack, x, y + size, 0F, size * 1F, size, height - size * 2, size * 3, (height - size * 2) * 3)
            blit(
                stack,
                x + size,
                y + size,
                width - size * 2F,
                size * 1F,
                width - size * 2,
                height - size * 2,
                (width - size * 2) * 3,
                (height - size * 2) * 3
            )
            blit(
                stack,
                x + width - size,
                y + size,
                size * 2F,
                size * 1F,
                size,
                height - size * 2,
                size * 3,
                (height - size * 2) * 3
            )
        } else {
            blit(stack, x, y + size, 0F, height - size * 2F, size, height - size * 2, size * 3, (height - size * 2) * 3)
            blit(
                stack,
                x + size,
                y + size,
                width - size * 2F,
                height - size * 2F,
                width - size * 2,
                height - size * 2,
                (width - size * 2) * 3,
                (height - size * 2) * 3
            )
            blit(
                stack,
                x + width - size,
                y + size,
                size * 2F,
                height - size * 2F,
                size,
                height - size * 2,
                size * 3,
                (height - size * 2) * 3
            )
        }

        blit(stack, x, y + height - size, 0F, size * 2F, size, size, size * 3, size * 3)
        blit(
            stack,
            x + size,
            y + height - size,
            width - size * 2F,
            size * 2F,
            width - size * 2,
            size,
            (width - size * 2) * 3,
            size * 3
        )
        blit(stack, x + width - size, y + height - size, size * 2F, size * 2F, size, size, size * 3, size * 3)

        stack.popPose()
    }

    @Suppress("DEPRECATION")
    fun drawBounds(stack: PoseStack, x1: Int, y1: Int, x2: Int, y2: Int, width: Int, color: Int) {
        val rgba = color.toRGBA()

        stack.use {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()

            GL11.glLineWidth(width.toFloat())
            RenderSystem.setShader { GameRenderer.getPositionTexShader() }
            val vb = Tesselator.getInstance().builder
            vb.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR)

            vb += VertexExt(stack, x1, y1, 0, rgba)
            vb += VertexExt(stack, x1, y2, 0, rgba)

            vb += VertexExt(stack, x1, y2, 0, rgba)
            vb += VertexExt(stack, x2, y2, 0, rgba)

            vb += VertexExt(stack, x2, y2, 0, rgba)
            vb += VertexExt(stack, x2, y1, 0, rgba)

            vb += VertexExt(stack, x2, y1, 0, rgba)
            vb += VertexExt(stack, x1, y1, 0, rgba)

            Tesselator.getInstance().end()

            GL11.glLineWidth(1f)
        }
    }

}