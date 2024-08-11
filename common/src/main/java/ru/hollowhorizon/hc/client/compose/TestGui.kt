package ru.hollowhorizon.hc.client.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.utils.literal
import ru.hollowhorizon.hc.client.utils.open
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.tick.TickEvent
import ru.hollowhorizon.hc.notesWindow
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent

class TestGui : Screen("".literal) {

    override fun init() {
        super.init()

        Renderer.onResize()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val mX = doubleArrayOf(0.0)
        val mY = doubleArrayOf(0.0)
        GLFW.glfwGetCursorPos(Minecraft.getInstance().window.window, mX, mY)
        Renderer.manager.sendPointerEvent(
            PointerEventType.Move,
            position = Offset(mX[0].toFloat(), mY[0].toFloat()),
            nativeEvent = MouseEvent(getAwtMods())
        )

        Renderer.render()
        //super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mX = doubleArrayOf(0.0)
        val mY = doubleArrayOf(0.0)
        GLFW.glfwGetCursorPos(Minecraft.getInstance().window.window, mX, mY)
        Renderer.manager.sendPointerEvent(
            PointerEventType.Press,
            position = Offset(mX[0].toFloat(), mY[0].toFloat()),
            nativeEvent = MouseEvent(getAwtMods())
        )

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mX = doubleArrayOf(0.0)
        val mY = doubleArrayOf(0.0)
        GLFW.glfwGetCursorPos(Minecraft.getInstance().window.window, mX, mY)
        Renderer.manager.sendPointerEvent(
            PointerEventType.Release,
            position = Offset(mX[0].toFloat(), mY[0].toFloat()),
            nativeEvent = MouseEvent(getAwtMods())
        )
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun getAwtMods(): Int {
        var n = 0

        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().window.window, GLFW.GLFW_MOUSE_BUTTON_LEFT) != 0) {
            n = n or InputEvent.BUTTON1_DOWN_MASK
        }
        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().window.window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) != 0) {
            n = n or InputEvent.BUTTON2_DOWN_MASK
        }
        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().window.window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) != 0) {
            n = n or InputEvent.BUTTON3_DOWN_MASK
        }
        if (hasControlDown()) {
            n = n or InputEvent.CTRL_DOWN_MASK
        }
        if (hasShiftDown()) {
            n = n or InputEvent.SHIFT_DOWN_MASK
        }
        if (hasAltDown()) {
            n = n or InputEvent.ALT_DOWN_MASK
        }
        return n
    }

    private val _dummy = object : Component() {}
    private fun MouseEvent(awtMods: Int) = MouseEvent(_dummy, 0, 0, awtMods, 0, 0, 1, false)

    override fun onClose() {
        super.onClose()
    }
}

//@SubscribeEvent
fun onTick(event: TickEvent.Client) {
    if (InputConstants.isKeyDown(event.minecraft.window.window, GLFW.GLFW_KEY_J)) {
        TestGui().open()
    }
}