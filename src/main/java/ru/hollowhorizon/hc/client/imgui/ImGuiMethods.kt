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

//? if <1.21 {
/*import net.minecraft.network.chat.contents.LiteralContents
*///?} else {
import net.minecraft.network.chat.contents.PlainTextContents
//?}
import com.google.common.collect.Queues
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexSorting
import imgui.ImFont
import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.extension.nodeditor.NodeEditor
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.locale.Language
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor.ARGB32
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import org.intellij.lang.annotations.MagicConstant
import org.joml.Matrix4f
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.imgui.addons.ItemProperties
import ru.hollowhorizon.hc.client.render.render
import ru.hollowhorizon.hc.client.render.renderItemDecorations
import ru.hollowhorizon.hc.client.utils.toTexture
import java.io.File
import java.util.*


object ImGuiMethods {
    internal val cursorStack: Deque<ImVec2> = Queues.newArrayDeque()
    val FONT_SIZES = HashMap<Int, ImFont>()

    var cursor: ImVec2
        get() = ImGui.getCursorPos()
        set(value) {
            ImGui.setCursorPos(value.x, value.y)
        }


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
            id, width, height, u0 / imageWidth, v0 / imageHeight, u1 / imageWidth, v1 / imageHeight, 1f, 1f, 1f, 1f
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
        pushColorStyles(
            ImGuiCol.CheckMark to innerColor, ImGuiCol.FrameBg to outerColor, ImGuiCol.FrameBgHovered to hoverColor
        ) {
            codeBlock(ImGuiMethods)
        }
    }

    fun progressBar(friction: Float) = ImGui.progressBar(friction)


    fun progressBar(friction: Float, sizeArgX: Float, sizeArgY: Float) = ImGui.progressBar(friction, sizeArgX, sizeArgY)


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

    inline fun pushColorStyles(
        vararg pairs: Pair<Int, Int>,
        codeBlock: ImGuiMethods.() -> Unit,
    ) {
        pairs.forEach { ImGui.pushStyleColor(it.first, it.second) }
        codeBlock(ImGuiMethods)
        ImGui.popStyleColor(pairs.count())
    }

    inline fun pushFont(font: ImFont, codeBlock: ImGuiMethods.() -> Unit) {
        ImGui.pushFont(font)
        codeBlock(ImGuiMethods)
        ImGui.popFont()
    }

    inline fun pushFontSize(size: Int, codeBlock: ImGuiMethods.() -> Unit) {
        pushFont(FONT_SIZES[size - size % 10] ?: return, codeBlock)
    }

    fun ImVec4.toFloatArray(): FloatArray = floatArrayOf(x, y, z, w)

    fun ImVec4.toRGB(): FloatArray {
        val container = floatArrayOf(0f, 0f, 0f)
        ImGui.colorConvertHSVtoRGB(this.toFloatArray(), container)
        return container
    }

    fun opengl(
        width: Float,
        height: Float,
        border: Boolean = false,
        red: Float = 1f,
        green: Float = 1f,
        blue: Float = 1f,
        alpha: Float = 1f,
        alwaysOnTop: Boolean,
        enableScissor: Boolean = true,
        isNodeEditor: Boolean = false,
        renderable: (ImVec2, Boolean) -> Unit,
    ): Boolean {
        val mcBuffer = Minecraft.getInstance().mainRenderTarget

        mcBuffer.unbindWrite()
        val buffer = currentBufferType.buffer
        buffer.bindWrite(true)

        // Без этого изображение будет обрезаться если его сдвинуть дальше одного экрана
        val cursor = if (isNodeEditor) ImVec2(
            NodeEditor.toScreenX(ImGui.getCursorScreenPosX()),
            NodeEditor.toScreenY(ImGui.getCursorScreenPosY())
        ) else ImGui.getCursorScreenPos()

        val isHovered = ImGui.isMouseHoveringRect(cursor.x, cursor.y, cursor.x + width, cursor.y + height)
        val isClicked = isHovered && ImGui.isMouseClicked(0)

        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(
            Matrix4f().setOrtho(
                0.0F, buffer.width.toFloat(), buffer.height.toFloat(), 0.0F, 1000.0F, 3000.0F
            ), VertexSorting.ORTHOGRAPHIC_Z
        )
        val matrix4fstack = RenderSystem.getModelViewStack()
        //? if <1.21 {
        /*matrix4fstack.pushPose()
        matrix4fstack.translate(0.0f, 0.0f, -2000.0f)
        *///?} else {
        matrix4fstack.pushMatrix()
        matrix4fstack.translation(0.0f, 0.0f, -2000.0f)
        //?}
        RenderSystem.applyModelViewMatrix()

        if (enableScissor) RenderSystem.enableScissor(
            cursor.x.toInt(), (buffer.height - cursor.y - height).toInt(),
            width.toInt(), (height).toInt(),
        )
        RenderSystem.enableDepthTest()

        renderable(cursor, isHovered)

        if (enableScissor) RenderSystem.disableScissor()
        RenderSystem.restoreProjectionMatrix()

        //? if <1.21 {
        /*matrix4fstack.popPose()
        *///?} else {
        matrix4fstack.popMatrix()
        //?}

        RenderSystem.applyModelViewMatrix()

        buffer.unbindWrite()
        mcBuffer.bindWrite(true)

        val u0 = cursor.x / buffer.width
        val u1 = (cursor.x + width) / buffer.width
        val v0 = 1f - cursor.y / buffer.height
        val v1 = 1f - (cursor.y + height) / buffer.height
        pushColorStyles(
            ImGuiCol.Button to ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 0f),
            ImGuiCol.ButtonActive to ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 0f),
            ImGuiCol.ButtonHovered to ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 0f),
        ) {
            val list = if (alwaysOnTop) ImGui.getForegroundDrawList() else ImGui.getWindowDrawList()

            val cursor = ImGui.getCursorScreenPos()

            list.addImage(
                buffer.colorTextureId, cursor.x, cursor.y,
                cursor.x + width, cursor.y + height, u0, v0, u1, v1,
                ImGui.colorConvertFloat4ToU32(red, green, blue, alpha)
            )
            if (border) list.addRect(
                cursor.x, cursor.y,
                cursor.x + width, cursor.y + height, -1
            )
        }

        return isClicked
    }

    fun exportFramebuffer() {
        val buffer = currentBufferType.buffer
        NativeImage(buffer.width, buffer.height, Minecraft.ON_OSX).apply {
            RenderSystem.setShaderTexture(
                0, buffer.colorTextureId
            ); downloadTexture(0, true)
        }.writeToFile(
            File("example.png")
        )
    }

    fun entity(
        entity: LivingEntity,
        width: Float,
        height: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        scale: Float = 1f,
        border: Boolean = false,
        rotation: Boolean = true,
        red: Float = 1f,
        green: Float = 1f,
        blue: Float = 1f,
        alpha: Float = 1f,
    ) {
        opengl(width, height, border, red, green, blue, alpha, false, renderable = { cursor, hovered ->
            val mouse = ImGui.getMousePos()

            entity.render(cursor.x, cursor.y, width, height, scale, mouse.x, mouse.y, offsetX, offsetY, rotation)
        })


    }

    fun item(
        item: ItemStack,
        width: Float,
        height: Float,
        name: String = item.hashCode().toString(),
        border: Boolean = false,
        properties: ItemProperties = ItemProperties(),
        isNodeEditor: Boolean = false,
    ): Boolean {
        ImGui.pushID(name)
        val cPos = ImGui.getCursorPos()
        val clicked = opengl(
            width,
            height,
            border,
            properties.red,
            properties.green,
            properties.blue,
            properties.alpha,
            properties.alwaysOnTop,
            isNodeEditor = isNodeEditor
        ) { cursor, hovered ->
            properties.update(hovered)
            var weight = item.count / item.maxStackSize.toFloat()
            if (item.maxStackSize == 1) weight = 0f
            val stack = PoseStack()
            if (properties.alwaysOnTop) stack.translate(0f, 0f, 200f)

            val enableCounts = HollowCore.config.inventory.enableItemCounts

            if (weight > 0.3f && enableCounts) {
                stack.pushPose()
                stack.translate(0f, 0f, -100f)
                item.render(
                    cursor.x + width / 5f, cursor.y, width, height,
                    (if (hovered || properties.disableResize) 1.0f else 0.9f) * properties.scale * 0.65f,
                    properties.rotation - 30f, stack
                )
                stack.popPose()
            }

            if (weight > 0.6f && enableCounts) {
                stack.pushPose()
                stack.translate(0f, 0f, -100f)
                item.render(
                    cursor.x - width / 5, cursor.y - height / 10, width, height,
                    (if (hovered || properties.disableResize) 1.0f else 0.9f) * properties.scale * 0.75f,
                    properties.rotation + 25f, stack
                )
                stack.popPose()
            }
            stack.pushPose()
            item.render(
                cursor.x, cursor.y, width, height,
                (if (hovered || properties.disableResize) 1.0f else 0.9f) * properties.scale, properties.rotation, stack
            )
            stack.popPose()
            renderItemDecorations(item, stack, cursor.x.toInt(), cursor.y.toInt(), width, height)
        }

        val player = Minecraft.getInstance().player ?: return false

        val pos = ImGui.getCursorScreenPos()

        if (ImGui.isMouseHoveringRect(
                pos.x,
                pos.y,
                pos.x + width,
                pos.y + height
            ) && !item.isEmpty && properties.tooltip
        ) {
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
            ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f)
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 0f)
            ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 1f, 1f)
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0f, 0f, 0f)
            tooltip {
                val borderSize = 5f
                val min = ImGui.getWindowPos()
                val max = min.clone() + ImGui.getWindowSize()
                var top = ImGui.colorConvertFloat4ToU32(0.19215688f, 0.09607843f, 0.45882353f, 1f).toLong()
                var bottom = ImGui.colorConvertFloat4ToU32(0.13725491f, 0.07058824f, 0.23921569f, 1f).toLong()

                ImGui.getForegroundDrawList().addRectFilled(min.x, min.y, max.x, min.y + borderSize, top.toInt())
                ImGui.getForegroundDrawList()
                    .addRectFilledMultiColor(min.x, min.y, min.x + borderSize, max.y, top, top, bottom, bottom)
                ImGui.getForegroundDrawList().addRectFilled(min.x, max.y - borderSize, max.x, max.y, bottom.toInt())
                ImGui.getForegroundDrawList()
                    .addRectFilledMultiColor(max.x - borderSize, min.y, max.x, max.y, top, top, bottom, bottom)

                top = ImGui.colorConvertFloat4ToU32(0.06f, 0.06f, 0.06f, 0.75f).toLong()
                bottom = ImGui.colorConvertFloat4ToU32(0.12f, 0.12f, 0.12f, 0.4f).toLong()
                ImGui.getWindowDrawList().addRectFilledMultiColor(
                    min.x + borderSize, min.y + borderSize, max.x - borderSize, max.y - borderSize,
                    top, top, top, top
                )

                ImGui.dummy(0f, borderSize / 2)
                //? if <1.21 {
                /*item.getTooltipLines(
                    player, TooltipFlag.Default.NORMAL
                ).forEach {
                    ImGui.setCursorPosX(ImGui.getCursorPosX() + borderSize * 2)
                    text(it)
                }
                *///?} else {
                item.getTooltipLines(
                    Item.TooltipContext.of(player.level()), player, TooltipFlag.Default.NORMAL
                ).forEach {
                    ImGui.setCursorPosX(ImGui.getCursorPosX() + borderSize * 2)
                    text(it)
                }
                //?}
                ImGui.dummy(borderSize, borderSize)
            }
            ImGui.popStyleColor(2)
            ImGui.popStyleVar(3)
        }

        if (item.count > 1) {
            val size = ImGui.calcTextSize(item.count.toString())

            val list = if (properties.alwaysOnTop) ImGui.getForegroundDrawList() else ImGui.getWindowDrawList()

            val cursor = ImGui.getCursorScreenPos() + ImVec2(width - size.x - 1, height - size.y - 1)
            val color = ImGui.getStyle().getColor(ImGuiCol.Text)
            list.addText(
                cursor.x + 2.5f,
                cursor.y + 2.5f,
                ImGui.colorConvertFloat4ToU32(color.x * 0.5f, color.y * 0.5f, color.z * 0.5f, color.w * 0.5f),
                item.count.toString()
            )
            list.addText(
                cursor.x, cursor.y,
                ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, color.w),
                item.count.toString()
            )

        }
        if (!properties.alwaysOnTop) {
            ImGui.setCursorPos(cPos.x, cPos.y)
            ImGui.dummy(width, height)
        }
        ImGui.popID()
        return clicked
    }

    fun text(text: Component, alpha: Float = 1f, shadow: Boolean = true) {
        val color = text.style.color?.value
        if (color != null) {
            val r = ARGB32.red(color)
            val g = ARGB32.green(color)
            val b = ARGB32.blue(color)

            pushColorStyle(ImGuiCol.Text, ImGui.colorConvertFloat4ToU32(r / 255f, g / 255f, b / 255f, alpha)) {
                drawText(text, alpha, shadow)
            }
        } else {
            drawText(text, alpha, shadow)
        }
    }

    private fun drawText(text: Component, alpha: Float = 1f, shadow: Boolean) {
        when (val content = text.contents) {
            //? if <1.21 {
            /*is LiteralContents,
                *///?} else {
                
            is PlainTextContents
            //?}
            -> {
                if (shadow) textShadow(content.text())
                else text(content.text())
            }


            is TranslatableContents -> {
                val decomposed = String.format(
                    Language.getInstance().getOrDefault(content.key),
                    *content.args.map { if (it is Component) it.string else it }.toTypedArray()
                )
                if (shadow) textShadow(decomposed)
                else text(decomposed)
            }
        }


        text.style.clickEvent?.let {
            when (it.action) {
                ClickEvent.Action.OPEN_URL -> Util.getPlatform().openUri(it.value)
                ClickEvent.Action.OPEN_FILE -> Util.getPlatform().openFile(File(it.value))
                ClickEvent.Action.RUN_COMMAND -> Minecraft.getInstance().connection?.sendCommand(it.value)
                ClickEvent.Action.COPY_TO_CLIPBOARD -> Minecraft.getInstance().keyboardHandler.clipboard = it.value
                else -> throw UnsupportedOperationException("Unsupported click action: ${it.action}")
            }
        }

        val isHovered = ImGui.isItemHovered()


        text.siblings.forEach {
            val old = ImGui.getStyle().itemSpacing
            ImGui.getStyle().setItemSpacing(0f, old.y)
            sameLine()

            text(it, alpha)

            ImGui.getStyle().setItemSpacing(old.x, old.y)
        }

        if (isHovered) text.style.hoverEvent?.let {
            val name =
                //? if <1.21 {
                /*it.action.name
            *///?} else {
            it.action.serializedName
            //?}
            when (name) {
                "show_text" -> {
                    ImGui.beginTooltip()
                    text(it.getValue(HoverEvent.Action.SHOW_TEXT) ?: Component.empty())
                    ImGui.endTooltip()
                }

                "show_item" -> {
                    ImGui.beginTooltip()
                    it.getValue(HoverEvent.Action.SHOW_ITEM)?.let {
                        item(it.itemStack, 128f, 128f)
                        it.itemStack.getTooltipLines(
                            //? if >=1.21 {
                            Item.TooltipContext.of(Minecraft.getInstance().level),
                            //?}
                            Minecraft.getInstance().player,
                            TooltipFlag.Default.NORMAL
                        ).forEach(::text)
                    }
                    ImGui.sameLine()
                    ImGui.endTooltip()
                }

                "show_entity" -> {
                    ImGui.beginTooltip()
                    it.getValue(HoverEvent.Action.SHOW_ENTITY)?.let {
                        val entity = Minecraft.getInstance().level?.entitiesForRendering()
                            ?.find { a -> a.uuid == it.id } as? LivingEntity
                        //? if <1.21 {
                        /*if(it.name != null) text(it.name!!)
                        *///?} else {
                        if (it.name.isPresent) text(it.name.get())
                        //?}
                        if (entity != null) entity(entity, 128f, 128f)
                    }
                    ImGui.endTooltip()
                }
            }
        }
    }

    fun textShadow(text: String) {
        val cursor = ImGui.getCursorPos()
        ImGui.setCursorPos(cursor.x + 2.5f, cursor.y + 2.5f)
        val color = ImGui.getStyle().getColor(ImGuiCol.Text)
        ImGui.pushStyleColor(ImGuiCol.Text, color.x * 0.5f, color.y * 0.5f, color.z * 0.5f, color.w * 0.5f)
        text(text)
        ImGui.popStyleColor()
        ImGui.setCursorPos(cursor.x, cursor.y)
        text(text)
    }
}