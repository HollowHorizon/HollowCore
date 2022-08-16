package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.utils.toSTC

open class HollowWidget(x: Int, y: Int, width: Int, height: Int, text: ITextComponent) : Widget(x, y, width, height, text) {
    private val onMouseClickedEvents: ArrayList<(mouseX: Double, mouseY: Double, button: Int) -> Boolean> = ArrayList()
    private val onMouseReleasedEvents: ArrayList<(mouseX: Double, mouseY: Double, button: Int) -> Boolean> = ArrayList()
    private val onMouseDraggedEvents: ArrayList<(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) -> Boolean> =
        ArrayList()
    private val onMouseScrolledEvents: ArrayList<(mouseX: Double, mouseY: Double, scroll: Double) -> Boolean> =
        ArrayList()
    private val onMouseMovedEvents: ArrayList<(mouseX: Double, mouseY: Double) -> Unit> = ArrayList()
    private val onKeyPressedEvents: ArrayList<(keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean> = ArrayList()
    private val onKeyReleasedEvents: ArrayList<(keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean> = ArrayList()
    private val onCharTypedEvents: ArrayList<(character: Char, keyCode: Int) -> Boolean> = ArrayList()
    protected val widgets: MutableList<Widget> = ArrayList()
    protected val textureManager: TextureManager = Minecraft.getInstance().textureManager
    protected val font: FontRenderer = Minecraft.getInstance().font

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        for (widget in widgets) {
            widget.isFocused =
                mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.width && mouseY < widget.y + widget.height
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
        for (widget in widgets) {
            if (widget.isHovered) {
                return widget.mouseClicked(mouseX, mouseY, button)
            }
        }

        for (event in onMouseClickedEvents) {
            if (event(mouseX, mouseY, button)) return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (widget in widgets) {
            widget.mouseReleased(mouseX, mouseY, button)
        }

        for (event in onMouseReleasedEvents) {
            if (event(mouseX, mouseY, button)) return true
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        for (widget in widgets) {
            if (widget.isHovered) return widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        }

        for (event in onMouseDraggedEvents) {
            if (event(mouseX, mouseY, button, dragX, dragY)) return true
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        for (widget in widgets) {
            if (widget.isHovered) return widget.mouseScrolled(mouseX, mouseX, scroll)
        }

        for (event in onMouseScrolledEvents) {
            if (event(mouseX, mouseY, scroll)) return true
        }

        return super.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        for (widget in widgets) {
            if (widget.isHovered) widget.mouseMoved(mouseX, mouseX)
        }

        for (event in onMouseMovedEvents) {
            event(mouseX, mouseY)
        }

        super.mouseMoved(mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (widget in widgets) {
            if (widget.isHovered) return widget.keyPressed(keyCode, scanCode, modifiers)
        }

        for (event in onKeyPressedEvents) {
            if (event(keyCode, scanCode, modifiers)) return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (widget in widgets) {
            if (widget.isHovered) return widget.keyReleased(keyCode, scanCode, modifiers)
        }

        for (event in onKeyReleasedEvents) {
            if (event(keyCode, scanCode, modifiers)) return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(character: Char, p_231042_2_: Int): Boolean {
        for (widget in widgets) {
            if (widget.isHovered) return widget.charTyped(character, p_231042_2_)
        }

        for (event in onCharTypedEvents) {
            if (event(character, p_231042_2_)) return true
        }

        return super.charTyped(character, p_231042_2_)
    }

    open fun onMouseClicked(event: (mouseX: Double, mouseY: Double, button: Int) -> Boolean): HollowWidget {
        onMouseClickedEvents.add(event)
        return this
    }

    open fun onMouseReleased(event: (mouseX: Double, mouseY: Double, button: Int) -> Boolean): HollowWidget {
        onMouseReleasedEvents.add(event)
        return this
    }

    open fun onMouseDragged(event: (mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) -> Boolean): HollowWidget {
        onMouseDraggedEvents.add(event)
        return this
    }

    open fun onMouseScrolled(event: (mouseX: Double, mouseY: Double, scroll: Double) -> Boolean): HollowWidget {
        onMouseScrolledEvents.add(event)
        return this
    }

    open fun onMouseMoved(event: (mouseX: Double, mouseY: Double) -> Unit): HollowWidget {
        onMouseMovedEvents.add(event)
        return this
    }

    open fun onKeyPressed(event: (keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean): HollowWidget {
        onKeyPressedEvents.add(event)
        return this
    }

    open fun onKeyReleased(event: (keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean): HollowWidget {
        onKeyReleasedEvents.add(event)
        return this
    }

    open fun onCharTyped(event: (character: Char, keyCode: Int) -> Boolean): HollowWidget {
        onCharTypedEvents.add(event)
        return this
    }

    fun bind(modid: String, path: String) {
        textureManager.bind(ResourceLocation(modid, "textures/$path"))
    }

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
                widget.x = widget.x - lx + x
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
                widget.y = widget.y - ly + y
            } else {
                widget.y = widget.y - ly + y
            }
        }
    }
}