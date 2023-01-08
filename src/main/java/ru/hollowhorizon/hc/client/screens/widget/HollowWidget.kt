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

open class HollowWidget(x: Int, y: Int, width: Int, height: Int, text: ITextComponent) :
    Widget(x, y, width, height, text) {
    val widgets: MutableList<Widget> = ArrayList()
    protected val textureManager: TextureManager = Minecraft.getInstance().textureManager
    protected val font: FontRenderer = Minecraft.getInstance().font
    private var isInitialized = false

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        if (!isInitialized) {
            init()
            isInitialized = true
        }

        for (widget in widgets) {
            widget.render(stack, mouseX, mouseY, ticks)
        }
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
        if(!visible) return false

        var isClicked = false
        for (widget in widgets) {
            isClicked = widget.mouseClicked(mouseX, mouseY, button) || isClicked
        }

        return super.mouseClicked(mouseX, mouseY, button) || isClicked
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(!visible) return false

        var isReleased = false
        for (widget in widgets) {
            isReleased = widget.mouseReleased(mouseX, mouseY, button) || isReleased
        }

        return super.mouseReleased(mouseX, mouseY, button) || isReleased
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if(!visible) return false

        var isDragged = false
        for (widget in widgets) {
            isDragged = widget.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isDragged
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isDragged
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if(!visible) return false

        var isScrolled = false
        for (widget in widgets) {
            isScrolled = widget.mouseScrolled(mouseX, mouseY, scroll) || isScrolled
        }

        return super.mouseScrolled(mouseX, mouseY, scroll) || isScrolled
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if(!visible) return

        for (widget in widgets) {
            widget.mouseMoved(mouseX, mouseY)
        }

        super.mouseMoved(mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if(!visible) return false

        var isPressed = false
        for (widget in widgets) {
            isPressed = widget.keyPressed(keyCode, scanCode, modifiers) || isPressed
        }

        return super.keyPressed(keyCode, scanCode, modifiers) || isPressed
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if(!visible) return false

        var isReleased = false
        for (widget in widgets) {
            isReleased = widget.keyReleased(keyCode, scanCode, modifiers) || isReleased
        }

        return super.keyReleased(keyCode, scanCode, modifiers) || isReleased
    }

    override fun charTyped(character: Char, p_231042_2_: Int): Boolean {
        if(!visible) return false

        var isTyped = false
        for (widget in widgets) {
            isTyped = widget.charTyped(character, p_231042_2_) || isTyped
        }

        return super.charTyped(character, p_231042_2_) || isTyped
    }

    fun bind(modid: String, path: String) {
        textureManager.bind(ResourceLocation(modid, "textures/$path"))
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
        size: Float = 1.0f
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


}