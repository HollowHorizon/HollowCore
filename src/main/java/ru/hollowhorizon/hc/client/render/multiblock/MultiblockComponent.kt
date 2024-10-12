package ru.hollowhorizon.hc.client.render.multiblock

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import imgui.ImGui
import imgui.flag.ImGuiMouseButton
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import org.joml.Quaternionf
import org.joml.Vector3f
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.imgui.PROPERTIES
import ru.hollowhorizon.hc.client.imgui.animatable
import ru.hollowhorizon.hc.client.utils.RANDOM
import ru.hollowhorizon.hc.common.multiblock.Multiblock
import kotlin.math.max


fun Graphics.drawMultiblock(
    multiblock: Multiblock,
    width: Float,
    height: Float,
    red: Float = 1f,
    green: Float = 1f,
    blue: Float = 1f,
    alpha: Float = 1f,
    border: Boolean = false,
    alwaysOnTop: Boolean = false,
    layer: Int = 1000
) {
    button("Reset") {
        PROPERTIES.clear()
    }
    val source = Minecraft.getInstance().renderBuffers().bufferSource()
    val blockRenderer = Minecraft.getInstance().blockRenderer
    val modelRenderer = blockRenderer.modelRenderer

    glCanvas(width, height, border, red, green, blue, alpha, alwaysOnTop) { cursor, hovered ->
        var scale by animatable { 0.9f }

        val wheel = ImGui.getIO().mouseWheel
        if (wheel != 0f && hovered) {
            scale += if (wheel > 0) 0.1f else -0.1f
            if (scale < 0.025f) scale = 0.025f
        }
        var rotX by animatable { 0f }
        var rotY by animatable { 0f }
        var offsetX by animatable { 0f }
        var offsetY by animatable { 0f }

        val mouse = ImGui.getIO().mouseDelta
        if (ImGui.isMouseDown(ImGuiMouseButton.Right) && hovered) {
            rotX += mouse.x
            rotY += mouse.y
        }
        if (ImGui.isMouseDown(ImGuiMouseButton.Middle) && hovered) {
            offsetX += mouse.x * 3
            offsetY += mouse.y * 3
        }

        val stack = PoseStack()

        val mbX = multiblock.xSize
        val mbY = multiblock.ySize
        val mbZ = multiblock.zSize

        val size = max(mbX, mbY)

        val xSize = width * scale / size
        val ySize = height * scale / size

        val pivotPoint = Vector3f(mbX / 2f, 0f, mbZ / 2f)

        stack.translate(
            cursor.x + width / 2.0 - xSize * size / 2 + offsetX,
            cursor.y + height / 2.0 + ySize * size / 2 + offsetY,
            0.0
        )
        stack.scale(xSize, -ySize, xSize)

        stack.translate(pivotPoint.x, pivotPoint.y, pivotPoint.z)

        val rotation = Quaternionf()
            .rotateX(rotY * Mth.DEG_TO_RAD)
            .rotateY(rotX * Mth.DEG_TO_RAD)

        stack.mulPose(rotation)
        stack.translate(-pivotPoint.x, -pivotPoint.y, -pivotPoint.z)

        for (y in 0..<mbY) {
            if(y > layer) break
            for (z in 0..<mbZ) {
                for (x in 0..<mbX) {
                    val id = x + z * mbZ + y * mbZ * mbX
                    val block = multiblock.blocks[id].default()
                    stack.pushPose()
                    stack.translate(x.toDouble(), y.toDouble(), z.toDouble())

                    val buffer: VertexConsumer = source.getBuffer(ItemBlockRenderTypes.getChunkRenderType(block))
                    val pos = BlockPos(x, y, z)
                    modelRenderer.tesselateWithoutAO(
                        multiblock, blockRenderer.getBlockModel(block), block, pos,
                        stack, buffer, false, RANDOM, block.getSeed(pos), OverlayTexture.NO_OVERLAY
                    )

                    stack.popPose()
                }
            }
        }
        source.endBatch()
    }
}