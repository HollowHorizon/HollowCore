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

package ru.hollowhorizon.hc.client.screens


import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiComponent.blit
import net.minecraft.client.gui.GuiComponent.fill
import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.GL11
import ru.hollowhorizon.hc.client.utils.toRGBA

object DrawUtils {
    @Suppress("DEPRECATION")
    fun drawBox(stack: PoseStack, location: ResourceLocation, x: Int, y: Int, width: Int, height: Int, color: Int) {
        val rgba = color.toRGBA()

        stack.pushPose()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        RenderSystem.setShaderColor(rgba.r, rgba.g, rgba.b, rgba.a)

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

        fill(stack, x1, y1, x1 + 1, y2, color)
        fill(stack, x1, y1, x2, y1 + 1, color)
        fill(stack, x2, y2, x1 + 1, y2 - 1, color)
        fill(stack, x2, y2, x2 - 1, y1, color)

    }

}