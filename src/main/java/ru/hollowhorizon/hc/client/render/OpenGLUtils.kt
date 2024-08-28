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

//? if >=1.21 {
/*import net.minecraft.util.FastColor
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.ItemDisplayContext
import com.mojang.blaze3d.vertex.VertexConsumer
*///?} elif >=1.20.1 {
/*import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
*///?} else {
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.block.model.ItemTransforms
import ru.hollowhorizon.hc.client.utils.math.mulPoseMatrix
import ru.hollowhorizon.hc.client.utils.math.vertex
import ru.hollowhorizon.hc.client.utils.fromMc
//?}
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3d
import ru.hollowhorizon.hc.client.handlers.TickHandler
import kotlin.math.atan
import kotlin.math.min

object OpenGLUtils {
    //? if <=1.19.2 {
    fun drawLine(bufferbuilder: BufferBuilder, matrix: com.mojang.math.Matrix4f,
                 from: Vector3d, to: Vector3d,
                 r: Float, g: Float, b: Float, a: Float): Unit = drawLine(bufferbuilder, matrix.fromMc(), from, to, r, g, b, a)
    //?}

    fun drawLine(
        bufferbuilder: BufferBuilder, matrix: Matrix4f,
        from: Vector3d, to: Vector3d,
        r: Float, g: Float, b: Float, a: Float,
    ) {
        //? if >=1.21 {
        /*bufferbuilder.addVertex(matrix, from.x.toFloat(), from.y.toFloat() - 0.1f, from.z.toFloat())
                    .setColor(r, g, b, a)
                bufferbuilder.addVertex(matrix, to.x.toFloat(), to.y.toFloat() - 0.1f, to.z.toFloat()).setColor(r, g, b, a)
        *///?} else {
        bufferbuilder.vertex(matrix, from.x.toFloat(), from.y.toFloat() - 0.1f, from.z.toFloat())
            .color(r, g, b, a)
        bufferbuilder.vertex(matrix, to.x.toFloat(), to.y.toFloat() - 0.1f, to.z.toFloat()).color(r, g, b, a)
        //?}
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
    rotation: Boolean,
) {
    val rotationFactor = if (rotation) 1f else 0f

    val stack = PoseStack()
    val xOffset = x + width / 2 + offsetX
    val yOffset = y + height + offsetY
    //? if >=1.20.1 {
    /*stack.translate(xOffset, yOffset, 0f)
    *///?} else {
    stack.translate(xOffset.toDouble(), yOffset.toDouble(), 0.0)
    //?}
    val newScale = min(width / bbWidth, height / bbHeight) * 0.95f * scale
    stack.scale(newScale, -newScale, newScale)

    val rotationX = atan((xOffset - mouseX) / 150.0f) * rotationFactor
    val rotationY = atan((yOffset - height / 2 - mouseY) / 150.0f) * rotationFactor

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
    val old = isCustomNameVisible
    isCustomNameVisible = false
    renderDispatcher.setRenderShadow(false)
    RenderSystem.runAsFancy {
        renderDispatcher.render(
            this, 0.0, 0.0, 0.0, 0.0f, 1.0f, stack, Minecraft.getInstance().renderBuffers().bufferSource(), 15728880
        )
    }

    RenderSystem.disableDepthTest()
    Minecraft.getInstance().renderBuffers().bufferSource().endBatch()
    RenderSystem.enableDepthTest()

    renderDispatcher.setRenderShadow(true)
    isCustomNameVisible = old
    yBodyRot = yBodyRotOld
    yRot = yRotOld
    xRot = xRotOld
    yHeadRot = yHeadRotOld
    yHeadRotO = yHeadRotOOld
    Lighting.setupFor3DItems()
}

fun ItemStack.render(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    scale: Float = 1f,
    rotation: Float = 0f,
    stack: PoseStack = PoseStack(),
) {
    val xOffset = x + width / 2
    val yOffset = y + height / 2
    //? if >=1.20.1 {
    /*stack.translate(xOffset, yOffset, 0f)
    *///?} else {
    stack.translate(xOffset.toDouble(), yOffset.toDouble(), 0.0)
    //?}

    //? if >=1.21 {
    /*stack.mulPose(Matrix4f().scaling(1f, -1f, 1f))
    *///?} else {
    stack.mulPoseMatrix(Matrix4f().scaling(1f, -1f, 1f))
    //?}

    val newScale = min(width, height) * 0.95f * scale
    stack.scale(newScale, newScale, newScale)
    //? if >=1.20.1 {
    /*stack.mulPose(Quaternionf().rotateZ(rotation * Mth.DEG_TO_RAD))
    *///?} else {
    val q = Quaternionf().rotateZ(rotation * Mth.DEG_TO_RAD)
    stack.mulPose(com.mojang.math.Quaternion(q.x, q.y, q.z, q.w))
    //?}


    val src = Minecraft.getInstance().renderBuffers().bufferSource()
    val model = Minecraft.getInstance().itemRenderer.getModel(this, Minecraft.getInstance().level, null, 0)

    val flat = !model.usesBlockLight()

    if (flat) Lighting.setupForFlatItems()
    Minecraft.getInstance().itemRenderer.render(
        this,
        //? if >=1.20.1 {
        /*ItemDisplayContext.GUI,
        *///?} else {
        ItemTransforms.TransformType.GUI,
        //?}

        false,
        stack, src, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model
    )

    src.endBatch()
    if (flat) Lighting.setupFor3DItems()
}

fun renderItemDecorations(stack: ItemStack, poseStack: PoseStack, x: Int, y: Int, width: Float, height: Float) {
    //? if >=1.20.1 {
    
    /*if (stack.isBarVisible) {
        val i = stack.barWidth / 16f
        val j = stack.barColor
        val k = (x + width * 0.125f).toInt()
        val l = (y + height * 0.8125f).toInt()
        fill(poseStack, RenderType.guiOverlay(), k, l, (k + width * 0.8125f).toInt(), (l + height * 0.125f).toInt(), 0, -16777216)
        fill(
            poseStack, RenderType.guiOverlay(), k, l, (k + i * width).toInt(),
            (l + height * 0.0625f).toInt(), 10,
            j or -16777216
        )
    }
    Minecraft.getInstance().player?.cooldowns?.getCooldownPercent(
        stack.item, TickHandler.partialTick
    )?.let { f ->
        if (f > 0) {
            val k = y + width * (Mth.floor(16.0f * (1.0f - f)) / 16f)
            val l = k + height * Mth.ceil(16.0f * f) / 16f
            fill(poseStack, RenderType.guiOverlay(), x, k.toInt(), (x + width).toInt(), l.toInt(), 0, Int.MAX_VALUE)
        }
    }
    *///?} else {
    if (!stack.isEmpty && stack.isBarVisible) {
        val i = stack.barWidth / 16f
        val j = stack.barColor
        val k = (x + width * 0.125f).toInt()
        val l = (y + height * 0.8125f).toInt()
        Screen.fill(poseStack, k, l, (k + width * 0.8125f).toInt(), (l + height * 0.125f).toInt(), -16777216)
        Screen.fill(poseStack, k, l, (k + i * width).toInt(), (l + height * 0.0625f).toInt(), j or -16777216)
    }
    Minecraft.getInstance().player?.cooldowns?.getCooldownPercent(
        stack.item, TickHandler.partialTick
    )?.let { f ->
        if (f > 0) {
            val k = y + width * (Mth.floor(16.0f * (1.0f - f)) / 16f)
            val l = k + height * Mth.ceil(16.0f * f) / 16f
            Screen.fill(poseStack, x, k.toInt(), (x + width).toInt(), l.toInt(), Int.MAX_VALUE)
        }
    }
    //?}
}

//? if >=1.20.1 {
/*fun fill(stack: PoseStack, renderType: RenderType, minX: Int, minY: Int, maxX: Int, maxY: Int, z: Int, color: Int) {
    var minX = minX
    var minY = minY
    var maxX = maxX
    var maxY = maxY
    var i: Int
    val matrix4f: Matrix4f = stack.last().pose()
    if (minX < maxX) {
        i = minX
        minX = maxX
        maxX = i
    }
    if (minY < maxY) {
        i = minY
        minY = maxY
        maxY = i
    }
    val src = Minecraft.getInstance().renderBuffers().bufferSource()
    val vertexConsumer: VertexConsumer = src.getBuffer(renderType)

    //? if <1.21 {
    /^vertexConsumer.vertex(matrix4f, minX.toFloat(), minY.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, minX.toFloat(), maxY.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, maxX.toFloat(), maxY.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, maxX.toFloat(), minY.toFloat(), z.toFloat()).color(color)
    ^///?} else {
    
    vertexConsumer.addVertex(matrix4f, minX.toFloat(), minY.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, minX.toFloat(), maxY.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, maxX.toFloat(), maxY.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, maxX.toFloat(), minY.toFloat(), z.toFloat()).setColor(color)

    RenderSystem.disableDepthTest()
    src.endBatch()
    RenderSystem.enableDepthTest()
    //?}
}
*///?}