package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.Screen
import ru.hollowhorizon.hc.client.ultralight.UltralightEngine
import ru.hollowhorizon.hc.client.ultralight.ViewOverlay
import ru.hollowhorizon.hc.client.utils.mc

class HTMLScreen : HollowScreen() {
    var view: ViewOverlay? = null

    override fun init() {
        super.init()

        UltralightEngine.init()

        if (view != null) UltralightEngine.removeView(view!!)
        else {
            view = UltralightEngine.newScreenView(this, this, this)

            view?.resize(width.toLong(), height.toLong())
            view?.loadUrl("file:///c:/Users/user/Desktop/papka_with_papkami/MyJavaProjects/HollowCore/src/main/resources/assets/hc/screen/index.html")

            UltralightEngine.inputAdapter.focusCallback(mc.window.window, true)
        }
    }

    override fun render(p_230430_1_: MatrixStack, p_230430_2_: Int, p_230430_3_: Int, p_230430_4_: Float) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_)

        view?.update()
        view?.render(p_230430_1_)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        UltralightEngine.inputAdapter.mouseButtonCallback(mc.window.window, button, 1, 0)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        UltralightEngine.inputAdapter.mouseButtonCallback(mc.window.window, button, 0, 0)
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (!Screen.hasShiftDown()) UltralightEngine.inputAdapter.scrollCallback(mc.window.window, 0.0, scroll)
        else UltralightEngine.inputAdapter.scrollCallback(mc.window.window, scroll, 0.0)

        return super.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        UltralightEngine.inputAdapter.cursorPosCallback(mc.window.window, mouseX, mouseY)
        super.mouseMoved(mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, mods: Int): Boolean {
        UltralightEngine.inputAdapter.keyCallback(mc.window.window, keyCode, scanCode, 1, mods)

        return super.keyPressed(keyCode, scanCode, mods)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, mods: Int): Boolean {
        UltralightEngine.inputAdapter.keyCallback(mc.window.window, keyCode, scanCode, 0, mods)

        return super.keyReleased(keyCode, scanCode, mods)
    }

    override fun onClose() {
        super.onClose()
        if (this.view != null) UltralightEngine.removeView(this.view!!)
    }

    override fun charTyped(p_231042_1_: Char, p_231042_2_: Int): Boolean {
        UltralightEngine.inputAdapter.charCallback(mc.window.window, p_231042_1_.code)

        return super.charTyped(p_231042_1_, p_231042_2_)
    }
}