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

//? if >=1.21 {
import com.mojang.blaze3d.vertex.VertexSorting
import net.minecraft.network.chat.contents.PlainTextContents
//?} elif >=1.20.1 {
/*
import net.minecraft.network.chat.contents.LiteralContents*/
//?} else {
/*import net.minecraft.network.chat.contents.LiteralContents
import ru.hollowhorizon.hc.client.utils.toMc
*///?}

import com.google.common.collect.Queues
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import imgui.*
import imgui.ImGui.*
import imgui.extension.nodeditor.NodeEditor
import imgui.flag.*
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.locale.Language
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
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
import ru.hollowhorizon.hc.client.imgui.Graphics.Style
import ru.hollowhorizon.hc.client.imgui.addons.ItemProperties
import ru.hollowhorizon.hc.client.render.render
import ru.hollowhorizon.hc.client.render.renderItemDecorations
import ru.hollowhorizon.hc.client.utils.literal
import ru.hollowhorizon.hc.client.utils.toTexture
import java.io.File
import java.util.*


object Graphics {
    internal val cursorStack: Deque<ImVec2> = Queues.newArrayDeque()
    val FONT_SIZES = HashMap<Int, ImFont>()

    val screenWidth get() = Minecraft.getInstance().window.width
    val screenHeight get() = Minecraft.getInstance().window.height


    fun pushCursor() = cursorStack.push(getCursorPos())
    fun popCursor() = cursorStack.pop().apply { setCursorPos(x, y) }

    fun pushScreenCursor() = cursorStack.push(getCursorScreenPos())
    fun popScreenCursor() = cursorStack.pop().apply { setCursorScreenPos(x, y) }


    fun sameLine(startX: Float) = ImGui.sameLine(startX)
    fun sameLine() = ImGui.sameLine()

    fun newLine() = ImGui.newLine()

    inline fun centredWindow(
        name: String = "Centred Window",
        @MagicConstant(valuesFromClass = ImGuiWindowFlags::class) args: Int = ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.AlwaysAutoResize,
        codeBlock: Graphics.() -> Unit,
    ) {
        if (begin(name, args)) {
            centerWindow()
            codeBlock()
        }
        end()
    }

    fun centerWindow() {
        setWindowPos(
            screenWidth / 2 - getWindowWidth() / 2,
            screenHeight / 2 - getWindowHeight() / 2
        )
    }

    inline fun window(
        windowName: String,
        @MagicConstant(valuesFromClass = ImGuiWindowFlags::class) imGuiWindowFlags: Int = 0,
        codeBlock: Graphics.() -> Unit,
    ) {
        begin(windowName, imGuiWindowFlags)
        codeBlock(Graphics)
        end()
    }

    inline fun child(strID: String, width: Float, height: Float, codeBlock: Graphics.() -> Unit) {
        beginChild(strID, width, height)
        codeBlock(Graphics)
        endChild()
    }

    fun bulletText(text: String) = ImGui.bulletText(text)

    fun button(name: String, codeBlock: Graphics.() -> Unit) {
        if (button(name)) codeBlock(Graphics)
    }

    fun button(name: String, width: Float, height: Float, codeBlock: Graphics.() -> Unit) {
        if (button(name, width, height)) codeBlock(Graphics)
    }

    fun arrowButton(
        name: String,
        @MagicConstant(valuesFromClass = ImGuiDir::class) dir: Int,
        codeBlock: Graphics.() -> Unit,
    ) {
        if (arrowButton(name, dir)) codeBlock(Graphics)
    }

    fun image(
        texture: ResourceLocation,
        width: Float,
        height: Float,
        imageWidth: Float = width,
        imageHeight: Float = height,
        u0: Float = 0f, v0: Float = 0f,
        u1: Float = width, v1: Float = height,
    ) {
        val id = texture.toTexture().id

        image(id, width, height, u0 / imageWidth, v0 / imageHeight, u1 / imageWidth, v1 / imageHeight, 1f, 1f, 1f, 1f)
    }

    fun image(textureId: Int, sizeX: Float, sizeY: Float, flip: Boolean = false) {
        if (flip) image(textureId, sizeX, sizeY, 0f, 1f, 1f, 0f)
        else image(textureId, sizeX, sizeY)
    }

    inline fun imageButton(textureId: Int, sizeX: Float, sizeY: Float, codeBlock: Graphics.() -> Unit) {
        if (imageButton(textureId, sizeX, sizeY)) codeBlock(Graphics)
    }

    inline fun imageButton(texture: ResourceLocation, sizeX: Float, sizeY: Float, codeBlock: Graphics.() -> Unit) {
        if (imageButton(texture.toTexture().id, sizeX, sizeY)) codeBlock(Graphics)
    }

    inline fun imageFlipButton(textureID: Int, sizeX: Float, sizeY: Float, codeBlock: Graphics.() -> Unit) {
        if (imageButton(textureID, sizeX, sizeY, 0f, 1f, 1f, 0f)) codeBlock(Graphics)
    }

    inline fun imageFlipButton(
        texture: ResourceLocation,
        sizeX: Float,
        sizeY: Float,
        codeBlock: Graphics.() -> Unit,
    ) {
        if (imageButton(texture.toTexture().id, sizeX, sizeY, 0f, 1f, 1f, 0f)) codeBlock(Graphics)
    }

    inline fun checkBox(name: String, isActive: Boolean, codeBlock: Graphics.() -> Unit) {
        if (checkbox(name, isActive)) codeBlock(Graphics)
    }

    inline fun radioButton(name: String, isActive: Boolean, codeBlock: Graphics.() -> Unit) {
        if (radioButton(name, isActive)) codeBlock(Graphics)
    }

    fun progressBar(progress: Float) = ImGui.progressBar(progress)
    fun progressBar(progress: Float, sizeArgX: Float, sizeArgY: Float) = ImGui.progressBar(progress, sizeArgX, sizeArgY)
    fun progressBar(progress: Float, sizeArgX: Float, sizeArgY: Float, overlay: String) =
        ImGui.progressBar(progress, sizeArgX, sizeArgY, overlay)

    fun bullet(num: Int) = repeat(num) { bullet() }

    inline fun combo(name: String, previewValue: String, codeBlock: Graphics.() -> Unit) {
        if (beginCombo(name, previewValue)) {
            codeBlock(Graphics)
            endCombo()
        }
    }

    inline fun node(name: String, codeBlock: Graphics.() -> Unit) {
        if (treeNode(name, name)) {
            codeBlock(Graphics)
            treePop()
        }
    }

    inline fun nodeEx(name: String, flags: Int = 0, codeBlock: Graphics.() -> Unit) {
        if (treeNodeEx(name, flags)) {
            codeBlock(Graphics)
            if ((ImGuiTreeNodeFlags.NoTreePushOnOpen and flags) == 0) treePop()
        }
    }

    inline fun collapsingHeader(name: String, codeBlock: Graphics.() -> Unit) {
        if (collapsingHeader(name)) codeBlock(Graphics)
    }

    inline fun listBox(name: String, codeBlock: Graphics.() -> Unit) {
        if (beginListBox(name)) {
            codeBlock(Graphics)
            endListBox()
        }
    }

    inline fun menuBar(codeBlock: Graphics.() -> Unit) {
        if (beginMenuBar()) {
            codeBlock(Graphics)
            endMenuBar()
        }
    }

    inline fun menu(name: String, codeBlock: Graphics.() -> Unit) {
        if (beginMenu(name)) {
            codeBlock(Graphics)
            endMenu()
        }
    }

    inline fun menuItem(name: String, codeBlock: Graphics.() -> Unit) {
        if (menuItem(name)) codeBlock(Graphics)
    }

    inline fun menuItem(name: String, shortcut: String, codeBlock: Graphics.() -> Unit) {
        if (menuItem(name, shortcut)) codeBlock(Graphics)
    }

    inline fun menuItem(name: String, shortcut: String, selected: Boolean, codeBlock: Graphics.() -> Unit) {
        if (menuItem(name, shortcut, selected)) codeBlock(Graphics)
    }

    inline fun menuItem(
        name: String,
        shortcut: String,
        selected: Boolean,
        enabled: Boolean,
        codeBlock: Graphics.() -> Unit,
    ) {
        if (menuItem(name, shortcut, selected, enabled)) codeBlock(Graphics)
    }


    inline fun tooltip(codeBlock: Graphics.() -> Unit) {
        beginTooltip()
        codeBlock(Graphics)
        endTooltip()
    }

    inline fun tooltipHover(codeBlock: Graphics.() -> Unit) {
        if (isItemHovered()) {
            beginTooltip()
            codeBlock(Graphics)
            endTooltip()
        }
    }

    inline fun tooltipHover(itemBlock: (Graphics) -> Unit, codeBlock: Graphics.() -> Unit) {
        itemBlock(Graphics)
        tooltipHover(codeBlock)
    }

    inline fun tabBar(name: String, codeBlock: Graphics.() -> Unit) {
        if (beginTabBar(name)) {
            codeBlock(Graphics)
            endTabBar()
        }
    }

    inline fun tabItem(name: String, codeBlock: Graphics.() -> Unit) {
        if (beginTabItem(name)) {
            codeBlock(Graphics)
            endTabItem()
        }
    }

    inline fun table(tableID: String, coloumNum: Int, codeBlock: Graphics.() -> Unit) {
        if (beginTable(tableID, coloumNum)) {
            codeBlock(Graphics)
            endTable()
        }
    }

    inline fun tableItem(codeBlock: Graphics.() -> Unit) {
        tableNextColumn()
        codeBlock(Graphics)
    }

    fun tableHeader(headerName: String) {
        tableItem { ImGui.tableHeader(headerName) }
    }

    inline fun indent(codeBlock: Graphics.() -> Unit) {
        indent()
        codeBlock(Graphics)
        unindent()
    }

    inline fun indent(indentWidth: Float, codeBlock: Graphics.() -> Unit) {
        indent(indentWidth)
        codeBlock(Graphics)
        unindent(indentWidth)
    }

    inline fun elementLeftClicked(codeBlock: Graphics.() -> Unit) {
        if (isItemClicked(0)) codeBlock(Graphics)
    }

    inline fun elementRightClicked(codeBlock: Graphics.() -> Unit) {
        if (isItemClicked(1)) codeBlock(Graphics)
    }

    inline fun elementMiddleClicked(codeBlock: Graphics.() -> Unit) {
        if (isItemClicked(2)) codeBlock(Graphics)
    }

    inline fun elementDoubleLeftClicked(codeBlock: Graphics.() -> Unit) {
        if (isItemHovered() && isMouseDoubleClicked(0)) codeBlock(Graphics)
    }

    inline fun elementDoubleRightClicked(codeBlock: Graphics.() -> Unit) {
        if (isItemHovered() && isMouseDoubleClicked(1)) codeBlock(Graphics)
    }

    inline fun popup(strID: String, codeBlock: Graphics.() -> Unit) {
        if (beginPopup(strID)) {
            codeBlock(Graphics)
            endPopup()
        }
    }

    inline fun pushId(id: Int, codeBlock: Graphics.() -> Unit) {
        pushID(id)
        codeBlock(Graphics)
        popID()
    }

    fun color(r: Float, g: Float, b: Float, a: Float) = colorConvertFloat4ToU32(r, g, b, a)
    fun color(color: Int) = color(
        ARGB32.red(color) / 255f,
        ARGB32.green(color) / 255f,
        ARGB32.blue(color) / 255f,
        ARGB32.alpha(color) / 255f
    )

    fun color(color: Long) = color(color.toInt())

    fun interface Style {
        fun apply()

        fun clear() = popStyleVar()
    }

    fun style(style: Int, value: Float) = Style {
        pushStyleVar(style, value)
    }

    fun style(style: Int, first: Float, second: Float) = Style {
        pushStyleVar(style, first, second)
    }

    fun styleColor(style: Int, color: Int) = object : Style {
        override fun apply() {
            pushStyleColor(style, color)
        }

        override fun clear() {
            popStyleColor()
        }
    }

    inline fun withColors(
        vararg pairs: Pair<Int, Int>,
        codeBlock: Graphics.() -> Unit,
    ) {
        pairs.forEach { pushStyleColor(it.first, it.second) }
        codeBlock(Graphics)
        popStyleColor(pairs.count())
    }

    inline fun withStyles(
        vararg pairs: Style,
        codeBlock: Graphics.() -> Unit,
    ) {
        pairs.forEach { it.apply() }
        codeBlock(Graphics)
        pairs.forEach { it.clear() }
    }

    inline fun withFont(font: ImFont, codeBlock: Graphics.() -> Unit) {
        pushFont(font)
        codeBlock(Graphics)
        popFont()
    }

    inline fun withFontSize(size: Int, codeBlock: Graphics.() -> Unit) {
        withFont(FONT_SIZES[size - size % 10] ?: return, codeBlock)
    }

    fun glCanvas(
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
        renderable: (cursor: ImVec2, isHovered: Boolean) -> Unit,
    ): Boolean {
        val mcBuffer = Minecraft.getInstance().mainRenderTarget

        mcBuffer.unbindWrite()
        val buffer = currentBufferType.buffer
        buffer.bindWrite(true)

        // Без этого изображение будет обрезаться если его сдвинуть дальше одного экрана
        val cursor = if (isNodeEditor) ImVec2(
            NodeEditor.canvasToScreenX(getCursorScreenPos()),
            NodeEditor.canvasToScreenY(getCursorScreenPos())
        ) else getCursorScreenPos()

        val isHovered = isMouseHoveringRect(cursor.x, cursor.y, cursor.x + width, cursor.y + height)
        val isClicked = isHovered && isMouseClicked(0)

        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(
            //? if >=1.20.1 {
            Matrix4f().setOrtho(
                0.0F, buffer.width.toFloat(), buffer.height.toFloat(), 0.0F, 1000.0F, 3000.0F
            ), VertexSorting.ORTHOGRAPHIC_Z
            //?} else {
            /*Matrix4f().setOrtho(
                0.0F, buffer.width.toFloat(), buffer.height.toFloat(), 0.0F, 1000.0F, 3000.0F
            ).toMc()
            *///?}
        )
        val matrix4fstack = RenderSystem.getModelViewStack()
        //? if >=1.21 {
        matrix4fstack.pushMatrix()
        matrix4fstack.translation(0.0f, 0.0f, -2000.0f)
        //?} elif >=1.20.1 {

        /*matrix4fstack.pushPose()
        matrix4fstack.setIdentity()
        matrix4fstack.translate(0.0f, 0.0f, -2000.0f)
        *///?} else {
        /*matrix4fstack.pushPose()
        matrix4fstack.translate(0.0, 0.0, -2000.0)
        *///?}
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
        withColors(
            ImGuiCol.Button to color(0f, 0f, 0f, 0f),
            ImGuiCol.ButtonActive to color(0f, 0f, 0f, 0f),
            ImGuiCol.ButtonHovered to color(0f, 0f, 0f, 0f),
        ) {
            val list = if (alwaysOnTop) getForegroundDrawList() else getWindowDrawList()

            val screenPos = getCursorScreenPos()

            list.addImage(
                buffer.colorTextureId, screenPos.x, screenPos.y,
                screenPos.x + width, screenPos.y + height, u0, v0, u1, v1,
                colorConvertFloat4ToU32(red, green, blue, alpha)
            )
            if (border) list.addRect(
                screenPos.x, screenPos.y,
                screenPos.x + width, screenPos.y + height, -1
            )
        }

        return isClicked
    }

    fun saveCurrentBuffer() {
        val buffer = currentBufferType.buffer
        NativeImage(buffer.width, buffer.height, Minecraft.ON_OSX).apply {
            RenderSystem.setShaderTexture(0, buffer.colorTextureId)
            downloadTexture(0, false)
        }.writeToFile(File("hollowcore/framebuffer_debug.png"))
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
        glCanvas(width, height, border, red, green, blue, alpha, false, renderable = { cursor, hovered ->
            val mouse = getMousePos()

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
        pushID(name)
        val cPos = getCursorPos()
        val clicked = glCanvas(
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
            if (properties.alwaysOnTop) {
                //? if >=1.21 {
                stack.translate(0f, 0f, 200f)
                //?} else {
                /*stack.translate(0.0, 0.0, 200.0)
                *///?}
            }

            val enableCounts = HollowCore.config.inventory.enableItemCounts

            if (weight > 0.3f && enableCounts) {
                stack.pushPose()
                //? if >=1.21 {
                stack.translate(0f, 0f, -100f)
                //?} else {
                /*stack.translate(0.0, 0.0, -100.0)
                *///?}
                item.render(
                    cursor.x + width / 5f, cursor.y, width, height,
                    (if (hovered || properties.disableResize) 1.0f else 0.9f) * properties.scale * 0.65f,
                    properties.rotation - 30f, stack
                )
                stack.popPose()
            }

            if (weight > 0.6f && enableCounts) {
                stack.pushPose()
                //? if >=1.21 {
                stack.translate(0f, 0f, -100f)
                //?} else {
                /*stack.translate(0.0, 0.0, -100.0)
                *///?}
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

        val pos = getCursorScreenPos()

        if (isMouseHoveringRect(
                pos.x,
                pos.y,
                pos.x + width,
                pos.y + height
            ) && !item.isEmpty && properties.tooltip
        ) {
            withStyles(
                style(ImGuiStyleVar.WindowPadding, 8f, 0f),
                style(ImGuiStyleVar.WindowRounding, 0f),
                style(ImGuiStyleVar.PopupBorderSize, 0f),
                styleColor(ImGuiCol.Border, color(WHITE)),
                styleColor(ImGuiCol.PopupBg, color(TRANSPARENT))
            ) {
                tooltip {
                    val borderSize = 5f
                    val min = getWindowPos()
                    val max = min.clone() + getWindowSize()
                    var top = colorConvertFloat4ToU32(0.19215688f, 0.09607843f, 0.45882353f, 1f)
                    var bottom = colorConvertFloat4ToU32(0.13725491f, 0.07058824f, 0.23921569f, 1f)

                    getForegroundDrawList().addRectFilled(min.x, min.y, max.x, min.y + borderSize, top.toInt())
                    getForegroundDrawList()
                        .addRectFilledMultiColor(min.x, min.y, min.x + borderSize, max.y, top, top, bottom, bottom)
                    getForegroundDrawList().addRectFilled(min.x, max.y - borderSize, max.x, max.y, bottom.toInt())
                    getForegroundDrawList()
                        .addRectFilledMultiColor(max.x - borderSize, min.y, max.x, max.y, top, top, bottom, bottom)

                    top = colorConvertFloat4ToU32(0.06f, 0.06f, 0.06f, 0.75f)
                    bottom = colorConvertFloat4ToU32(0.12f, 0.12f, 0.12f, 0.4f)
                    getWindowDrawList().addRectFilledMultiColor(
                        min.x + borderSize, min.y + borderSize, max.x - borderSize, max.y - borderSize,
                        top, top, top, top
                    )

                    dummy(0f, borderSize / 2)
                    //? if <1.21 {
                    /*item.getTooltipLines(
                        player, TooltipFlag.Default.NORMAL
                    ).forEach {
                        setCursorPosX(getCursorPosX() + borderSize * 2)
                        text(it)

                    }
                    *///?} else {
                    item.getTooltipLines(
                        Item.TooltipContext.of(player.level()), player, TooltipFlag.Default.NORMAL
                    ).forEach {
                        setCursorPosX(getCursorPosX() + borderSize * 2)
                        text(it)
                    }
                    //?}
                    dummy(borderSize, borderSize)
                }
            }
        }

        if (item.count > 1) {
            val size = calcTextSize(item.count.toString())

            val list = if (properties.alwaysOnTop) getForegroundDrawList() else getWindowDrawList()

            val cursor = getCursorScreenPos() + ImVec2(width - size.x - 1, height - size.y - 1)
            val color = getStyle().getColor(ImGuiCol.Text)
            list.addText(
                cursor.x + 2.5f,
                cursor.y + 2.5f,
                colorConvertFloat4ToU32(color.x * 0.5f, color.y * 0.5f, color.z * 0.5f, color.w * 0.5f),
                item.count.toString()
            )
            list.addText(
                cursor.x, cursor.y,
                colorConvertFloat4ToU32(color.x, color.y, color.z, color.w),
                item.count.toString()
            )

        }
        if (!properties.alwaysOnTop) {
            setCursorPos(cPos.x, cPos.y)
            dummy(width, height)
        }
        popID()
        return clicked
    }

    fun text(string: String, alpha: Float = 1f, shadow: Boolean = true) = text(string.literal, alpha, shadow)

    fun text(text: Component, alpha: Float = 1f, shadow: Boolean = true) {
        drawText(text, alpha, shadow)
    }

    private fun obfuscatedString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + ('А'..'Я') + ('а'..'я')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun drawText(text: Component, alpha: Float = 1f, shadow: Boolean) {
        val textColor = color(text.style.color?.value ?: colorConvertFloat4ToU32(getStyle().getColor(ImGuiCol.Text))) or ((alpha * 255f).toInt() shl 24)
        withColors(
            ImGuiCol.Text to textColor
        ) {
            val isUnderlined = text.style.isUnderlined
            val isStrikethrough = text.style.isStrikethrough
            val isObfuscated = text.style.isObfuscated

            when (val content = text.contents) {
                //? if <1.21 {
                /*is LiteralContents,
                    *///?} else {

                    is PlainTextContents,
                        //?}
                -> {
                    val string = if (isObfuscated) obfuscatedString(content.text().length) else content.text()
                    val length = calcTextSize(string)
                    val startPos = getCursorScreenPos()

                    if (shadow) textShadow(string)
                    else ImGui.text(string)

                    if (isUnderlined || isStrikethrough) {
                        val size = ImGui.getFontSize() / 30f
                        val offset = if (isUnderlined) length.y else length.y / 2f + 3f * size
                        getWindowDrawList().addLine(
                            startPos.x - size,
                            startPos.y + offset,
                            startPos.x + length.x + size,
                            startPos.y + offset,
                            textColor,
                            3f * size
                        )
                    }
                }


                is TranslatableContents -> {
                    val decomposed = String.format(
                        Language.getInstance().getOrDefault(content.key),
                        *content.args.map { if (it is Component) it.string else it }.toTypedArray()
                    )
                    val string = if (isObfuscated) obfuscatedString(decomposed.length) else decomposed
                    if (shadow) textShadow(string)
                    else ImGui.text(string)
                }

                //? if<=1.20.1 {
                /*ComponentContents.EMPTY -> {
                    newLine()
                }*///?}
            }


            text.style.clickEvent?.let {
                when (it.action) {
                    ClickEvent.Action.OPEN_URL -> Util.getPlatform().openUri(it.value)
                    ClickEvent.Action.OPEN_FILE -> Util.getPlatform().openFile(File(it.value))
                    ClickEvent.Action.RUN_COMMAND -> {
                        val connection = Minecraft.getInstance().connection
                        if (connection != null) {
                            //? if >=1.20.1 {
                            connection.sendCommand(it.value)
                            //?} else {
                            /*connection.commands.execute(it.value, connection.suggestionsProvider)
                            *///?}
                        }
                    }

                    ClickEvent.Action.COPY_TO_CLIPBOARD -> Minecraft.getInstance().keyboardHandler.clipboard = it.value
                    else -> throw UnsupportedOperationException("Unsupported click action: ${it.action}")
                }
            }

            val isHovered = isItemHovered()


            text.siblings.forEach {
                val old = getStyle().itemSpacing
                withStyles(style(ImGuiStyleVar.ItemSpacing, 0f, old.y)) {
                    sameLine()

                    text(it, alpha)
                }
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
                        beginTooltip()
                        text(it.getValue(HoverEvent.Action.SHOW_TEXT) ?: Component.empty())
                        endTooltip()
                    }

                    "show_item" -> {
                        beginTooltip()
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
                        endTooltip()
                    }

                    "show_entity" -> {
                        beginTooltip()
                        it.getValue(HoverEvent.Action.SHOW_ENTITY)?.let {
                            val entity = Minecraft.getInstance().level?.entitiesForRendering()
                                ?.find { a -> a.uuid == it.id } as? LivingEntity
                            //? if <1.21 {
                            /*if (it.name != null) text(it.name!!)
                            *///?} else {
                            if (it.name.isPresent) text(it.name.get())
                            //?}
                            if (entity != null) entity(entity, 128f, 128f)
                        }
                        endTooltip()
                    }
                }
            }
        }
    }

    fun textShadow(text: String) {
        val cursor = getCursorPos()
        setCursorPos(cursor.x + 2.5f, cursor.y + 2.5f)
        val color = getStyle().getColor(ImGuiCol.Text)
        pushStyleColor(ImGuiCol.Text, color.x * 0.5f, color.y * 0.5f, color.z * 0.5f, color.w * 0.5f)
        ImGui.text(text)
        popStyleColor()
        setCursorPos(cursor.x, cursor.y)
        ImGui.text(text)
    }

    fun ImDrawList.textShadow(
        x: Float,
        y: Float,
        text: String,
        font: ImFont = getFont(),
        size: Int = getFontSize(),
        color: Int = getStyle().getColor(ImGuiCol.Text).color,
        wrapWidth: Float = -1f,
    ) {
        val newColor = color.colorVec.times(0.5f, 0.5f, 0.5f, 0.5f).color

        if (wrapWidth == -1f) addText(font, size, ImVec2(x + 2.5f, y + 2.5f), newColor, text, null, wrapWidth)
        else addText(font, size, x + 2.5f, y + 2.5f, newColor, text)

        if (wrapWidth == -1f) addText(font, size, x, y, color, text, null, wrapWidth)
        else addText(font, size, x, y, color, text)
    }

    val ImVec4.color get() = color(x, y, z, w)
    val Int.colorVec: ImVec4 get() = colorConvertU32ToFloat4(this)
}