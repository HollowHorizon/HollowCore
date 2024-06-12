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

package ru.hollowhorizon.hc.client.render

import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Matrix4f
import org.joml.Vector3d
import kotlin.math.atan
import kotlin.math.min

object OpenGLUtils {
    fun drawLine(
        bufferbuilder: BufferBuilder, matrix: Matrix4f,
        from: Vector3d, to: Vector3d,
        r: Float, g: Float, b: Float, a: Float,
    ) {
        bufferbuilder
            .vertex(matrix, from.x.toFloat(), from.y.toFloat() - 0.1f, from.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
        bufferbuilder
            .vertex(matrix, to.x.toFloat(), to.y.toFloat() - 0.1f, to.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
    }
}


fun LivingEntity.render(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    scale: Float,
    mouseX: Float,
    mouseY: Float,
    offsetX: Float,
    offsetY: Float,
) {
    val stack = PoseStack()
    val xOffset = x + width / 2 + offsetX
    val yOffset = y + height + offsetY
    stack.translate(xOffset, yOffset, 0f)
    val newScale = min(width / bbWidth, height / bbHeight) * 0.95f * scale
    stack.scale(newScale, -newScale, newScale)

    val rotationX = atan((xOffset - mouseX) / 150.0f)
    val rotationY = atan((yOffset - height / 2 - mouseY) / 150.0f)

    Lighting.setupForEntityInInventory()
    val renderDispatcher = Minecraft.getInstance().entityRenderDispatcher

    val yBodyRotOld: Float = yBodyRot
    val yRotOld: Float = yRot
    val xRotOld: Float = xRot
    val yHeadRotOOld: Float = yHeadRotO
    val yHeadRotOld: Float = yHeadRot
    yBodyRot = rotationX * 20f
    yRot = rotationX * 40f
    xRot = -rotationY * 20f
    yHeadRot = yRot
    yHeadRotO = yRot
    renderDispatcher.setRenderShadow(false)
    RenderSystem.runAsFancy {
        renderDispatcher.render(
            this, 0.0, 0.0, 0.0, 0.0f, 1.0f,
            stack, Minecraft.getInstance().renderBuffers().bufferSource(),
            15728880
        )
    }
    RenderSystem.disableDepthTest()
    Minecraft.getInstance().renderBuffers().bufferSource().endBatch()
    RenderSystem.enableDepthTest()
    renderDispatcher.setRenderShadow(true)
    yBodyRot = yBodyRotOld
    yRot = yRotOld
    xRot = xRotOld
    yHeadRot = yHeadRotOld
    yHeadRotO = yHeadRotOOld
    Lighting.setupFor3DItems()
}

fun ItemStack.render(x: Float, y: Float, width: Float, height: Float, scale: Float) {
    val stack = PoseStack()
    val xOffset = x + width / 2
    val yOffset = y + height / 2
    stack.translate(xOffset, yOffset, 0f)
    val newScale = min(width, height) * 0.95f * scale
    stack.scale(newScale, -newScale, newScale)

    val src = Minecraft.getInstance().renderBuffers().bufferSource()
    val model = Minecraft.getInstance().itemRenderer.getModel(this, Minecraft.getInstance().level, null, 0)
    val flat = !model.usesBlockLight()

    if (flat) Lighting.setupForFlatItems()
    Minecraft.getInstance().itemRenderer.render(
        this,
        ItemDisplayContext.GUI,
        false,
        stack,
        src,
        LightTexture.FULL_BRIGHT,
        OverlayTexture.NO_OVERLAY,
        model
    )
    src.endBatch()
    if (flat) Lighting.setupFor3DItems()
}