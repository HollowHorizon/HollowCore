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

package ru.hollowhorizon.hc.client.imgui

import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Matrix4f
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import ru.hollowhorizon.hc.client.render.render
import ru.hollowhorizon.hc.client.utils.toTexture
import ru.hollowhorizon.hc.common.ui.Alignment
import kotlin.math.abs
import kotlin.math.min

val buffers = ArrayList<TextureTarget>()
val imguiBuffer = TextureTarget(512, 512, true, Minecraft.ON_OSX)

fun framebuffer(width: Float, height: Float) =
    TextureTarget(width.toInt(), height.toInt(), true, Minecraft.ON_OSX).apply {
        buffers += this
    }

fun opengl(width: Float, height: Float, border: Boolean = false, renderable: (ImVec2) -> Unit) {
    val mcBuffer = Minecraft.getInstance().mainRenderTarget

    mcBuffer.unbindWrite()
    imguiBuffer.bindWrite(true)

    val cursor = ImGui.getCursorScreenPos()
    ImGui.getMousePos()
    RenderSystem.backupProjectionMatrix()
    val m = Matrix4f()
    m.setIdentity()
    RenderSystem.setProjectionMatrix(
        Matrix4f.orthographic(imguiBuffer.width.toFloat(), -imguiBuffer.height.toFloat(), 1000.0f, 3000.0f)
    )

    RenderSystem.enableScissor(
        cursor.x.toInt(), (imguiBuffer.height - cursor.y - height).toInt(),
        width.toInt(), (height).toInt(),
    )
    RenderSystem.enableDepthTest()

    renderable(cursor)

    RenderSystem.disableScissor()
    RenderSystem.restoreProjectionMatrix()

    imguiBuffer.unbindWrite()
    mcBuffer.bindWrite(true)

    val u0 = cursor.x / imguiBuffer.width
    val u1 = (cursor.x + width) / imguiBuffer.width
    val v0 = 1f - cursor.y / imguiBuffer.height
    val v1 = 1f - (cursor.y + height) / imguiBuffer.height
    if (!border) ImGui.image(imguiBuffer.colorTextureId, width, height, u0, v0, u1, v1)
    else ImGui.image(imguiBuffer.colorTextureId, width, height, u0, v0, u1, v1, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)
}

fun entity(entity: LivingEntity, width: Float, height: Float) {
    opengl(width, height) { cursor ->
        val mouse = ImGui.getMousePos()
        val scale = min(width / entity.bbWidth, height / entity.bbHeight)
        entity.render(
            (cursor.x + width / 2).toInt(),
            (cursor.y + height).toInt(),
            scale * 0.9f,
            (cursor.x + width / 2 - mouse.x) / 10,
            (cursor.y + height / 2 - mouse.y) / 10
        )
    }


}

fun item(item: ItemStack, width: Float, height: Float, border: Boolean = false) {
    opengl(width, height, border) { cursor ->
        val minecraft = Minecraft.getInstance()
        val itemRenderer = minecraft.itemRenderer
        val font = minecraft.font


        val modelView = RenderSystem.getModelViewStack()
        modelView.pushPose()
        modelView.translate(cursor.x + width / 2.0, cursor.y + height / 2.0, 0.0)

        val scale = min(width / 16, height / 16)
        modelView.scale(scale, scale, 0f)
        itemRenderer.renderAndDecorateItem(item, -8, -8)
        itemRenderer.renderGuiItemDecorations(font, item, -8, -8, item.count.toString())
        modelView.popPose()

    }

    val player = Minecraft.getInstance().player ?: return
    //val advanced = player::isShiftKeyDown
    if (ImGui.isItemHovered()) {
        ImGui.setTooltip(item.getTooltipLines(player, TooltipFlag.Default.NORMAL).map { it.string }.joinToString("\n"))
    }
}

fun align(alignment: Alignment = Alignment.CENTER, renderable: () -> Unit) {
    val avail = ImGui.getContentRegionAvail()
    val original = ImGui.getCursorPos()
    ImGui.setCursorPos(-10000.0f, -10000.0f)
    renderable()
    val ySize = ImGui.getCursorPos() - ImVec2(-10000.0f, -10000.0f)
    ImGui.sameLine()
    val pos = ImGui.getCursorPos() - ImVec2(-10000.0f, -10000.0f) + ImVec2(0f, ySize.y) - ImGui.getStyle().itemSpacing

    val size = ImVec2((avail.x - pos.x) * alignment.factorX, (avail.y - abs(pos.y)) * alignment.factorY)

    ImGui.setCursorPos(original.x + size.x, original.y + size.y)

    renderable()

    ImGui.setCursorPos(original.x, original.y)
}

fun centredWindow(
    name: String = "Centred Window",
    args: Int = ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar,
    draw: () -> Unit,
) {
    if (ImGui.begin(name, args)) {
        centerWindow()
        draw()
    }
    ImGui.end()
}

fun centerWindow() {
    ImGui.setWindowPos(
        Minecraft.getInstance().window.width.toFloat() / 2 - ImGui.getWindowWidth() / 2,
        Minecraft.getInstance().window.height.toFloat() / 2 - ImGui.getWindowHeight() / 2
    )
}

fun image(
    texture: ResourceLocation,
    width: Float,
    height: Float,
    imageWidth: Float = width,
    imageHeight: Float = height,
    u0: Float = 0f,
    v0: Float = 0f,
    u1: Float = width,
    v1: Float = height,
) {
    val id = texture.toTexture().id

    ImGui.image(id, width, height, u0 / imageWidth, v0 / imageHeight, u1 / imageWidth, v1 / imageHeight, 1f, 1f, 1f, 1f)
}