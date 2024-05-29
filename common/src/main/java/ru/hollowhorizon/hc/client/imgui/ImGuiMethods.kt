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

import com.google.common.collect.Queues
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexSorting
import imgui.ImFont
import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import org.intellij.lang.annotations.MagicConstant
import org.joml.Matrix4f
import ru.hollowhorizon.hc.client.render.render
import ru.hollowhorizon.hc.client.utils.toTexture
import java.util.*
import kotlin.math.min

object ImGuiMethods {
    internal val cursorStack: Deque<ImVec2> = Queues.newArrayDeque()

    fun pushCursor() = cursorStack.push(ImGui.getCursorPos())
    fun popCursor() = cursorStack.pop().apply { ImGui.setCursorPos(x, y) }

    fun sameLine() = ImGui.sameLine()

    fun sameLine(startX: Float) = ImGui.sameLine(startX)

    inline fun centredWindow(
        name: String = "Centred Window",
        @MagicConstant(valuesFromClass = ImGuiWindowFlags::class) args: Int = ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.AlwaysAutoResize,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        if (ImGui.begin(name, args)) {
            centerWindow()
            codeBlock()
        }
        ImGui.end()
    }

    fun centerWindow() {
        ImGui.setWindowPos(
            Minecraft.getInstance().window.width.toFloat() / 2 - ImGui.getWindowWidth() / 2,
            Minecraft.getInstance().window.height.toFloat() / 2 - ImGui.getWindowHeight() / 2
        )
    }

    inline fun window(windowName: String, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.begin(windowName)
        codeBlock(ImGuiMethods)
        ImGui.end()
    }

    inline fun window(
        windowName: String,
        @MagicConstant(valuesFromClass = ImGuiWindowFlags::class) imGuiWindowFlags: Int,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        ImGui.begin(windowName, imGuiWindowFlags)
        codeBlock(ImGuiMethods)
        ImGui.end()
    }

    inline fun area(strID: String, width: Float, height: Float, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.beginChild(strID, width, height)
        codeBlock(ImGuiMethods)
        ImGui.endChild()
    }

    fun bulletText(text: String) = ImGui.bulletText(text)

    fun button(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.button(name)) codeBlock(ImGuiMethods)
    }

    fun button(name: String, width: Float, height: Float, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.button(name, width, height)) codeBlock(ImGuiMethods)
    }

    fun arrowButton(
        name: String,
        @MagicConstant(valuesFromClass = ImGuiDir::class) dir: Int,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        if (ImGui.arrowButton(name, dir)) codeBlock(ImGuiMethods)
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

        ImGui.image(
            id,
            width,
            height,
            u0 / imageWidth,
            v0 / imageHeight,
            u1 / imageWidth,
            v1 / imageHeight,
            1f,
            1f,
            1f,
            1f
        )
    }

    fun image(textureId: Int, sizeX: Float, sizeY: Float) {
        ImGui.image(textureId, sizeX, sizeY)
    }

    fun image(texture: ResourceLocation, sizeX: Float, sizeY: Float) {
        ImGui.image(texture.toTexture().id, sizeX, sizeY)
    }

    fun imageFlip(textureID: Int, sizeX: Float, sizeY: Float) {
        ImGui.image(textureID, sizeX, sizeY, 0f, 1f, 1f, 0f)
    }

    fun imageFlip(texture: ResourceLocation, sizeX: Float, sizeY: Float) {
        ImGui.image(texture.toTexture().id, sizeX, sizeY, 0f, 1f, 1f, 0f)
    }

    inline fun imageButton(textureId: Int, sizeX: Float, sizeY: Float, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.imageButton(textureId, sizeX, sizeY)) codeBlock(ImGuiMethods)
    }

    inline fun imageButton(texture: ResourceLocation, sizeX: Float, sizeY: Float, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.imageButton(texture.toTexture().id, sizeX, sizeY)) codeBlock(ImGuiMethods)
    }

    inline fun imageFlipButton(textureID: Int, sizeX: Float, sizeY: Float, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.imageButton(textureID, sizeX, sizeY, 0f, 1f, 1f, 0f)) codeBlock(ImGuiMethods)
    }

    inline fun imageFlipButton(
        texture: ResourceLocation,
        sizeX: Float,
        sizeY: Float,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        if (ImGui.imageButton(texture.toTexture().id, sizeX, sizeY, 0f, 1f, 1f, 0f)) codeBlock(ImGuiMethods)

    }

    inline fun checkBox(name: String, isActive: Boolean, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.checkbox(name, isActive)) codeBlock(ImGuiMethods)
    }

    inline fun radioButton(name: String, isActive: Boolean, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.radioButton(name, isActive)) codeBlock(ImGuiMethods)
    }

    inline fun coloredRadioButton(
        name: String,
        isActive: Boolean,
        innerColor: Int,
        outerColor: Int,
        hoverColor: Int,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        pushColorStyle(ImGuiCol.CheckMark, innerColor) {
            pushColorStyle(ImGuiCol.FrameBg, outerColor) {
                pushColorStyle(ImGuiCol.FrameBgHovered, hoverColor) {
                    radioButton(name, isActive) {
                        codeBlock(ImGuiMethods)
                    }
                }
            }
        }
    }

    fun progressBar(friction: Float) =
        ImGui.progressBar(friction)


    fun progressBar(friction: Float, sizeArgX: Float, sizeArgY: Float) =
        ImGui.progressBar(friction, sizeArgX, sizeArgY)


    fun progressBar(friction: Float, sizeArgX: Float, sizeArgY: Float, overlay: String) =
        ImGui.progressBar(friction, sizeArgX, sizeArgY, overlay)

    fun bullet() = ImGui.bullet()

    fun bullet(num: Int) = repeat(num) { ImGui.bullet() }

    inline fun combo(name: String, previewValue: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginCombo(name, previewValue)) {
            codeBlock(ImGuiMethods)
            ImGui.endCombo()
        }
    }

    inline fun node(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.treeNode(name)) {
            codeBlock(ImGuiMethods)
            ImGui.treePop()
        }
    }

    inline fun collapsingHeader(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.collapsingHeader(name)) codeBlock(ImGuiMethods)
    }

    inline fun listBox(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginListBox(name)) {
            codeBlock(ImGuiMethods)
            ImGui.endListBox()
        }
    }

    inline fun menuBar(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginMenuBar()) {
            codeBlock(ImGuiMethods)
            ImGui.endMenuBar()
        }
    }

    inline fun menu(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginMenu(name)) {
            codeBlock(ImGuiMethods)
            ImGui.endMenu()
        }
    }

    inline fun menuItem(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.menuItem(name)) codeBlock(ImGuiMethods)
    }

    inline fun menuItem(name: String, shortcut: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.menuItem(name, shortcut)) codeBlock(ImGuiMethods)
    }

    inline fun menuItem(name: String, shortcut: String, selected: Boolean, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.menuItem(name, shortcut, selected)) codeBlock(ImGuiMethods)
    }

    inline fun menuItem(
        name: String,
        shortcut: String,
        selected: Boolean,
        enabled: Boolean,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        if (ImGui.menuItem(name, shortcut, selected, enabled)) codeBlock(ImGuiMethods)
    }


    inline fun tooltip(codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.beginTooltip()
        codeBlock(ImGuiMethods)
        ImGui.endTooltip()
    }

    inline fun tooltipHover(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip()
            codeBlock(ImGuiMethods)
            ImGui.endTooltip()
        }
    }

    inline fun tooltipHover(itemBlock: (ImGuiMethods) -> Unit, codeBlock: ImGuiMethods.() -> Unit) {
        itemBlock(ImGuiMethods)
        tooltipHover(codeBlock)
    }

    /**
     * use [tabItem] in this scope
     */
    inline fun tab(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginTabBar(name)) {
            codeBlock(ImGuiMethods)
            ImGui.endTabBar()
        }
    }

    /**
     * used in scope of [tab]
     */
    inline fun tabItem(name: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginTabItem(name)) {
            codeBlock(ImGuiMethods)
            ImGui.endTabItem()
        }
    }

    /**
     * use [tableItem] in this scope
     *
     * table use see [github](https://github.com/ocornut/imgui/issues/3740#issuecomment-764882290)
     */
    inline fun table(tableID: String, coloumNum: Int, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginTable(tableID, coloumNum)) {
            codeBlock(ImGuiMethods)
            ImGui.endTable()
        }
    }

    inline fun tableItem(codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.tableNextColumn()
        codeBlock(ImGuiMethods)
    }

    fun tableHeader(headerName: String) {
        tableItem { ImGui.tableHeader(headerName) }
    }

    inline fun indent(codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.indent()
        codeBlock(ImGuiMethods)
        ImGui.unindent()
    }

    inline fun indent(indentWidth: Float, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.indent(indentWidth)
        codeBlock(ImGuiMethods)
        ImGui.unindent(indentWidth)
    }

    inline fun leftClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemClicked(0)) codeBlock(ImGuiMethods)
    }

    inline fun rightClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemClicked(1)) codeBlock(ImGuiMethods)
    }

    inline fun middleClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemClicked(2)) codeBlock(ImGuiMethods)
    }

    inline fun doubleLeftClickClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) codeBlock(ImGuiMethods)
    }

    inline fun doubleRightClickClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(1)) codeBlock(ImGuiMethods)
    }

    inline fun doubleMiddleClickClickLast(codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(2)) codeBlock(ImGuiMethods)
    }

    fun setPopupEnable(strID: String) = ImGui.openPopup(strID)

    inline fun popup(strID: String, codeBlock: ImGuiMethods.() -> Unit) {
        if (ImGui.beginPopup(strID)) {
            codeBlock(ImGuiMethods)
            ImGui.endPopup()
        }
    }

    fun text(text: String) {
        ImGui.text(text)
    }

    fun coloredText(text: String, color: Int) {
        ImGui.textColored(color, text)
    }

    fun coloredText(text: String, red: Int, green: Int, blue: Int, alpha: Int) {
        ImGui.textColored(red, green, blue, alpha, text)
    }

    fun coloredText(text: String, red: Float, green: Float, blue: Float, alpha: Float) {
        ImGui.textColored(red, green, blue, alpha, text)
    }

    inline fun pushId(id: Int, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.pushID(id)
        codeBlock(ImGuiMethods)
        ImGui.popID()
    }

    inline fun pushColorStyle(
        @MagicConstant(valuesFromClass = ImGuiCol::class) imGuiCol: Int,
        color: Int,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        ImGui.pushStyleColor(imGuiCol, color)
        codeBlock(ImGuiMethods)
        ImGui.popStyleColor()
    }

    inline fun pushFont(font: ImFont, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.pushFont(font)
        codeBlock(ImGuiMethods)
        ImGui.popFont()
    }

    fun ImVec4.toFloatArray(): FloatArray =
        floatArrayOf(x, y, z, w)

    fun ImVec4.toRGB(): FloatArray {
        val container = floatArrayOf(0f, 0f, 0f)
        ImGui.colorConvertHSVtoRGB(this.toFloatArray(), container)
        return container
    }

    fun opengl(width: Float, height: Float, border: Boolean = false, renderable: (ImVec2) -> Unit) {
        val mcBuffer = Minecraft.getInstance().mainRenderTarget

        mcBuffer.unbindWrite()
        imguiBuffer.bindWrite(true)

        val cursor = ImGui.getCursorScreenPos()
        ImGui.getMousePos()
        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(
            Matrix4f().setOrtho(
                0.0F,
                imguiBuffer.width.toFloat(),
                imguiBuffer.height.toFloat(),
                0.0F,
                1000.0F,
                3000.0F
            ),
            VertexSorting.ORTHOGRAPHIC_Z
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

    fun entity(
        entity: LivingEntity,
        width: Float,
        height: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        scale: Float = 1f,
    ) {
        opengl(width, height) { cursor ->
            val mouse = ImGui.getMousePos()
            val size = min(width / entity.bbWidth, height / entity.bbHeight)
            entity.render(
                GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource()),
                (cursor.x + width / 2 + offsetX),
                (cursor.y + height + offsetY),
                size * scale * 0.9f,
                (cursor.x + width / 2 + offsetX - mouse.x) / 10,
                (cursor.y + height / 2 - offsetY - mouse.y) / 10
            )
        }


    }

    fun item(item: ItemStack, width: Float, height: Float, border: Boolean = false) {
        opengl(width, height, border) { cursor ->
            val minecraft = Minecraft.getInstance()
            val itemRenderer = minecraft.itemRenderer
            val font = minecraft.font


            val modelView = RenderSystem.getModelViewStack()
            modelView.pushMatrix()
            modelView.translate(cursor.x + width / 2.0f, cursor.y + height / 2.0f, 0.0f)

            val scale = min(width / 16, height / 16)
            modelView.scale(scale, scale, 0f)
            itemRenderer.renderStatic(
                item,
                ItemDisplayContext.GUI,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                PoseStack(),
                Minecraft.getInstance().renderBuffers().bufferSource(),
                Minecraft.getInstance().level!!,
                0
            )
            modelView.popMatrix()

        }

        val player = Minecraft.getInstance().player ?: return
        //val advanced = player::isShiftKeyDown
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(
                item.getTooltipLines(
                    Item.TooltipContext.of(player.level()),
                    player,
                    TooltipFlag.Default.NORMAL
                ).joinToString("\n") { it.string })
        }
    }
}