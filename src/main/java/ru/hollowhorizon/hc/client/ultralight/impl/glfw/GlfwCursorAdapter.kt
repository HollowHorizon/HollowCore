package ru.hollowhorizon.hc.client.ultralight.impl.glfw

import com.labymedia.ultralight.input.UltralightCursor
import ru.hollowhorizon.hc.client.ultralight.UltralightEngine
import org.lwjgl.glfw.GLFW

/**
 * Utility class for controlling GLFW cursors.
 */
class GlfwCursorAdapter {

    private val beamCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR)
    private val crosshairCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR)
    private val handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
    private val hresizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR)
    private val vresizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR)

    /**
     * Signals this [GlfwCursorAdapter] that the cursor has been updated and needs to be updated on the GLFW side
     * too.
     *
     * @param cursor The new [UltralightCursor] to display
     */
    fun notifyCursorUpdated(cursor: UltralightCursor?) {
        when (cursor) {
            UltralightCursor.CROSS -> GLFW.glfwSetCursor(UltralightEngine.window, crosshairCursor)
            UltralightCursor.HAND -> GLFW.glfwSetCursor(UltralightEngine.window, handCursor)
            UltralightCursor.I_BEAM -> GLFW.glfwSetCursor(UltralightEngine.window, beamCursor)
            UltralightCursor.EAST_WEST_RESIZE -> GLFW.glfwSetCursor(UltralightEngine.window, hresizeCursor)
            UltralightCursor.NORTH_SOUTH_RESIZE -> GLFW.glfwSetCursor(UltralightEngine.window, vresizeCursor)
            else -> GLFW.glfwSetCursor(UltralightEngine.window, 0)
        }
    }

    /**
     * Frees GLFW resources allocated by this [GlfwCursorAdapter].
     */
    fun cleanup() {
        GLFW.glfwDestroyCursor(vresizeCursor)
        GLFW.glfwDestroyCursor(hresizeCursor)
        GLFW.glfwDestroyCursor(handCursor)
        GLFW.glfwDestroyCursor(crosshairCursor)
        GLFW.glfwDestroyCursor(beamCursor)
    }

    fun unfocus() {
        GLFW.glfwSetCursor(UltralightEngine.window, 0)
    }

}
