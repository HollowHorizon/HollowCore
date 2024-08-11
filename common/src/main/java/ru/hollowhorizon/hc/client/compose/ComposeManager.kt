package ru.hollowhorizon.hc.client.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.Minecraft
import org.jetbrains.skia.Color
import org.jetbrains.skiko.currentNanoTime
import org.lwjgl.glfw.GLFW
import java.util.concurrent.Executors


@OptIn(InternalComposeUiApi::class)
class ComposeManager(
    private var width: Int,
    private var height: Int,
    content: @Composable () -> Unit,
) {
    var scene: ComposeScene = CanvasLayersComposeScene(
        Density(glfwGetWindowContentScale(Minecraft.getInstance().window.window)),
        coroutineContext = dispatcher
    ) {
        Renderer.execute {
            Renderer.surface.canvas.clear(Color.TRANSPARENT)
            Renderer.manager.scene.render(Renderer.canvas.asComposeCanvas(), System.nanoTime())
            Renderer.context.flush()
        }
    }.apply {
        setContent {
            content()
        }
        size = IntSize(width, height)
    }

    fun finalizeCompose() {
        scene.close()
    }

    fun sendPointerEvent(
        eventType: PointerEventType,
        position: Offset,
        scrollDelta: Offset = Offset(0f, 0f),
        timeMillis: Long = (currentNanoTime() / 1E6).toLong(),
        type: PointerType = PointerType.Mouse,
        buttons: PointerButtons? = null,
        keyboardModifiers: PointerKeyboardModifiers? = null,
        nativeEvent: Any? = null,
        button: PointerButton? = null,
    ) {

        Renderer.execute {
            scene.sendPointerEvent(
                eventType,
                position,
                scrollDelta,
                timeMillis,
                type,
                buttons,
                keyboardModifiers,
                nativeEvent,
                button
            )
        }
    }

    fun sendKeyEvent(event: KeyEvent) {
        Renderer.execute {
            scene.sendKeyEvent(event)
        }
    }


    fun resizeCanvas(widthNow: Int, heightNow: Int): Boolean {
        if (widthNow != width || heightNow != height) {
            scene.size = IntSize(widthNow, heightNow)
            width = widthNow
            height = heightNow
            return true
        }
        return false
    }

    companion object {
        val dispatcher = Renderer.asCoroutineDispatcher()
    }
}

fun glfwGetWindowContentScale(window: Long): Float {
    val array = FloatArray(1)
    GLFW.glfwGetWindowContentScale(window, array, FloatArray(1))
    return array[0]
}