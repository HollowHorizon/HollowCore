package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import ru.hollowhorizon.hc.client.utils.ScissorUtil
import ru.hollowhorizon.hc.client.utils.toSTC

open class OriginWidget(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "ORIGIN".toSTC()) {
    private var isLeftButtonPressed = false
    private var lastMouseX = 0
    private var lastMouseY = 0
    var originX = 0
    var originY = 0
    var scale = 1f
    var enableSliders = false
    var canMove = true
    open var canScale = true
    val maxHeight: Int
        get() {
            var min = this.y
            for (widget in widgets) {
                if (widget.y < min) min = widget.y
            }

            var max = min
            for (widget in widgets) {
                if (widget.y + widget.height > max) max = widget.y + widget.height
            }
            return max - min - this.height
        }
    val maxWidth: Int
        get() {
            val min = widgets.minOfOrNull { it.x } ?: this.y
            val max = widgets.maxOfOrNull { it.x + it.width } ?: min
            return max - min - this.width
        }


    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        val originMX = mouseX.toOriginX()
        val originMY = mouseY.toOriginY()

        stack.pushPose()
        stack.translate(-originX.toDouble(), -originY.toDouble(), 0.0)
        stack.scale(scale, scale, 1f)

        ScissorUtil.push(x, y, width, height)
        super.renderButton(stack, mouseX, mouseY, ticks)
        ScissorUtil.pop()

        stack.popPose()


        if (Screen.hasAltDown()) renderTooltipInternal(
            stack,
            listOf(
                ClientTooltipComponent.create(
                    FormattedCharSequence.forward(
                        "x$originMX y${-originMY}",
                        Style.EMPTY
                    )
                )
            ),
            mouseX,
            mouseY
        )
    }

    override fun renderWidget(widget: AbstractWidget, stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        if (widget is IOriginBlackList) {
            stack.pushPose()
            stack.scale(1 / scale, 1 / scale, 1f)
            stack.translate(originX.toDouble(), originY.toDouble(), 0.0)
            widget.render(stack, mouseX, mouseY, ticks)
            stack.popPose()
        } else {
            widget.render(stack, mouseX.toOriginX(), mouseY.toOriginY(), ticks)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!super.mouseClicked(mouseX, mouseY, button) && button == 0 && canMove && isHovered) {
            isLeftButtonPressed = true
            lastMouseX = mouseX.toInt()
            lastMouseY = mouseY.toInt()
        }
        return true
    }

    override fun widgetMouseClicked(widget: AbstractWidget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (widget is IOriginBlackList) {
            super.widgetMouseClicked(widget, mouseX, mouseY, button)
        } else {
            super.widgetMouseClicked(widget, mouseX.toOriginX(), mouseY.toOriginY(), button)
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && canMove) {
            isLeftButtonPressed = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun widgetMouseReleased(widget: AbstractWidget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (widget is IOriginBlackList) {
            super.widgetMouseReleased(widget, mouseX, mouseY, button)
        } else {
            super.widgetMouseReleased(widget, mouseX.toOriginX(), mouseY.toOriginY(), button)
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (isLeftButtonPressed && canMove) {
            originX += lastMouseX - mouseX.toInt()
            originY += lastMouseY - mouseY.toInt()

            lastMouseX = mouseX.toInt()
            lastMouseY = mouseY.toInt()
        }
        super.mouseMoved(mouseX, mouseY)
    }

    override fun widgetMouseMoved(widget: AbstractWidget, mouseX: Double, mouseY: Double) {
        if (widget is IOriginBlackList) {
            super.widgetMouseMoved(widget, mouseX, mouseY)
        } else {
            super.widgetMouseMoved(widget, mouseX.toOriginX(), mouseY.toOriginY())
        }
    }

    override fun widgetMouseDragged(
        widget: AbstractWidget,
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        return if (widget is IOriginBlackList) {
            super.widgetMouseDragged(widget, mouseX, mouseY, button, dragX, dragY)
        } else {
            super.widgetMouseDragged(widget, mouseX.toOriginX(), mouseY.toOriginY(), button, dragX, dragY)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (Screen.hasControlDown() && canScale) {
            scale += 0.1f * (scroll > 0).let { if (it) 1 else -1 }
            if (scale < 0.1f) scale = 0.1f
            if (scale > 10f) scale = 10f
        }
        return super.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun widgetMouseScrolled(widget: AbstractWidget, mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        return if (widget is IOriginBlackList) {
            super.widgetMouseScrolled(widget, mouseX, mouseY, scroll)
        } else {
            super.widgetMouseScrolled(widget, mouseX.toOriginX(), mouseY.toOriginY(), scroll)
        }
    }

    override fun playDownSound(p_230988_1_: SoundManager) {
        //звука не будет :)
    }

    private fun Int.toOriginX() = ((this + originX) / scale).toInt()
    private fun Int.toOriginY() = ((this + originY) / scale).toInt()

    private fun Double.toOriginX() = (this + originX) / scale
    private fun Double.toOriginY() = (this + originY) / scale
}