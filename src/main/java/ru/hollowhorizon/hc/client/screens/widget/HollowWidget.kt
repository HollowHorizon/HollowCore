package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.screens.widget.layout.ILayoutConsumer

open class HollowWidget(x: Int, y: Int, width: Int, height: Int, text: ITextComponent) :
    Widget(x, y, width, height, text), ILayoutConsumer {
    @JvmField
    val widgets = ArrayList<Widget>()
    protected val textureManager: TextureManager = Minecraft.getInstance().textureManager
    protected val font: FontRenderer = Minecraft.getInstance().font
    private var isInitialized = false

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        if (!isInitialized) {
            init()
            isInitialized = true
        }

        widgets.forEach { widget ->
            if (!widget.visible) return

            renderWidget(widget, stack, mouseX, mouseY, ticks)
        }
    }

    open fun renderWidget(widget: Widget, stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        widget.render(stack, mouseX, mouseY, ticks)
    }

    open fun init() {}
    fun <T : Widget> addWidget(widget: T): T {
        widgets.add(widget)
        return widget
    }

    fun addWidgets(vararg widgets: Widget) {
        this.widgets.addAll(listOf(*widgets))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        var isClicked = false
        for (widget in widgets) {
            if (widget.visible) isClicked = widgetMouseClicked(widget, mouseX, mouseY, button) || isClicked
        }

        return super.mouseClicked(mouseX, mouseY, button) || isClicked
    }

    open fun widgetMouseClicked(widget: Widget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widget.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        var isReleased = false
        for (widget in widgets) {
            if (widget.visible) isReleased = widgetMouseReleased(widget, mouseX, mouseY, button) || isReleased
        }

        return super.mouseReleased(mouseX, mouseY, button) || isReleased
    }

    open fun widgetMouseReleased(widget: Widget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widget.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!visible) return false

        var isDragged = false
        for (widget in widgets) {
            if (widget.visible) isDragged =
                widgetMouseDragged(widget, mouseX, mouseY, button, dragX, dragY) || isDragged
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isDragged
    }

    open fun widgetMouseDragged(
        widget: Widget,
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        return widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (!visible) return false

        var isScrolled = false
        for (widget in widgets) {
            if (widget.visible) isScrolled = widgetMouseScrolled(widget, mouseX, mouseY, scroll) || isScrolled
        }

        return super.mouseScrolled(mouseX, mouseY, scroll) || isScrolled
    }

    open fun widgetMouseScrolled(widget: Widget, mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        return widget.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (!visible) return

        for (widget in widgets) {
            if (widget.visible) widgetMouseMoved(widget, mouseX, mouseY)
        }

        super.mouseMoved(mouseX, mouseY)
    }

    open fun widgetMouseMoved(widget: Widget, mouseX: Double, mouseY: Double) {
        widget.mouseMoved(mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        var isPressed = false
        for (widget in widgets) {
            if (widget.visible) isPressed = widget.keyPressed(keyCode, scanCode, modifiers) || isPressed
        }

        return super.keyPressed(keyCode, scanCode, modifiers) || isPressed
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        var isReleased = false
        for (widget in widgets) {
            if (widget.visible) isReleased = widget.keyReleased(keyCode, scanCode, modifiers) || isReleased
        }

        return super.keyReleased(keyCode, scanCode, modifiers) || isReleased
    }

    override fun charTyped(character: Char, p_231042_2_: Int): Boolean {
        if (!visible) return false

        var isTyped = false
        for (widget in widgets) {
            if (widget.visible) isTyped = widget.charTyped(character, p_231042_2_) || isTyped
        }

        return super.charTyped(character, p_231042_2_) || isTyped
    }

    fun bind(modid: String, path: String) {
        textureManager.bind(ResourceLocation(modid, "textures/$path"))
    }

    fun bind(path: ResourceLocation) {
        textureManager.bind(path)
    }

    override fun playDownSound(p_230988_1_: SoundHandler) {}

    @JvmOverloads
    fun betterBlit(
        stack: MatrixStack,
        alignment: Alignment,
        offsetX: Int,
        offsetY: Int,
        targetWidth: Int,
        targetHeight: Int,
        imageWidth: Int = targetWidth,
        imageHeight: Int = targetHeight,
        texX: Int = 0,
        texY: Int = 0,
        size: Float = 1.0f,
    ) {
        blit(
            stack,
            HollowScreen.getAlignmentPosX(alignment, offsetX + this.x, width, targetWidth, size),
            HollowScreen.getAlignmentPosY(alignment, offsetY - this.y, height, targetHeight, size),
            texX.toFloat(),
            texY.toFloat(),
            (targetWidth * size).toInt(),
            (targetHeight * size).toInt(),
            (imageWidth * size).toInt(),
            (imageHeight * size).toInt()
        )
    }

    fun setX(x: Int) {
        val lx = this.x
        this.x = x
        for (widget in widgets) {
            if (widget is HollowWidget) {
                widget.setX(widget.x - lx + x)
            } else {
                widget.x = widget.x - lx + x
            }
        }
    }

    fun setY(y: Int) {
        val ly = this.y
        this.y = y
        for (widget in widgets) {
            if (widget is HollowWidget) {
                widget.setY(widget.y - ly + y)
            } else {
                widget.y = widget.y - ly + y
            }
        }
    }

    override fun addLayoutWidget(widget: Widget) {
        this.addWidget(widget)
    }

    override fun x() = this.x
    override fun y() = this.y
    override fun width() = this.width
    override fun height() = this.height

    fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height
    }

    open fun tick() {
        this.widgets.forEach { if(it is HollowWidget) it.tick() }
    }
}