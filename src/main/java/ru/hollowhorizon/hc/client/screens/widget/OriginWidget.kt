package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.fml.client.gui.GuiUtils
import ru.hollowhorizon.hc.client.screens.widget.button.ColorButton
import ru.hollowhorizon.hc.client.utils.ScissorUtil
import ru.hollowhorizon.hc.client.utils.parent
import ru.hollowhorizon.hc.client.utils.toSTC

class OriginWidget(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "ORIGIN".toSTC()) {
    private var isLeftButtonPressed = false
    private var lastMouseX = 0
    private var lastMouseY = 0
    var originX = 0
    var originY = 0
    var scale = 1f

    init {
        originX = x - width / 2
        originY = y - height / 2
    }

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        val originMX = mouseX.toOriginX()
        val originMY = mouseY.toOriginY()

        stack.pushPose()
        stack.translate(-originX.toDouble(), -originY.toDouble(), 0.0)
        stack.scale(scale, scale, 1f)

        super.renderButton(stack, originMX, originMY, ticks)
        stack.popPose()
        if (Screen.hasAltDown()) GuiUtils.drawHoveringText(
            stack,
            listOf("x$originMX y${-originMY}".toSTC()),
            mouseX,
            mouseY,
            width,
            height,
            -1,
            font
        )

    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && Screen.hasAltDown()) {
            isLeftButtonPressed = true
            lastMouseX = mouseX.toInt()
            lastMouseY = mouseY.toInt()
            return true
        }
        return super.mouseClicked(mouseX.toOriginX(), mouseY.toOriginY(), button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            isLeftButtonPressed = false
        }
        return super.mouseReleased(mouseX.toOriginX(), mouseY.toOriginY(), button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (isLeftButtonPressed) {
            originX += lastMouseX - mouseX.toInt()
            originY += lastMouseY - mouseY.toInt()

            lastMouseX = mouseX.toInt()
            lastMouseY = mouseY.toInt()
        }
        super.mouseMoved(mouseX.toOriginX(), mouseY.toOriginY())
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        return super.mouseDragged(mouseX.toOriginX(), mouseY.toOriginY(), button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (Screen.hasAltDown()) {
            scale += 0.1f * (scroll > 0).let { if (it) 1 else -1 }
            if (scale < 0.1f) scale = 0.1f
            if (scale > 10f) scale = 10f
        }
        return super.mouseScrolled(mouseX.toOriginX(), mouseY.toOriginY(), scroll)
    }

    override fun playDownSound(p_230988_1_: SoundHandler) {
        //звука не будет :)
    }

    private fun Int.toOriginX() = ((this + originX) / scale).toInt()
    private fun Int.toOriginY() = ((this + originY) / scale).toInt()

    private fun Double.toOriginX() = (this + originX) / scale
    private fun Double.toOriginY() = (this + originY) / scale
}