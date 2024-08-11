package ru.hollowhorizon.hc.client.compose

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asComposeCanvas
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import net.minecraft.client.Minecraft
import org.jetbrains.skia.*
import org.lwjgl.opengl.GL11
import ru.hollowhorizon.hc.notesWindow
import java.util.concurrent.Executor

object Renderer: Executor {
    val commands = ArrayList<Runnable>()
    private val mc = Minecraft.getInstance()

    lateinit var context: DirectContext
    private lateinit var renderTarget: BackendRenderTarget
    lateinit var manager: ComposeManager
    lateinit var surface: Surface
    lateinit var canvas: Canvas
    lateinit var targetFbo: RenderTarget

    @OptIn(InternalComposeUiApi::class)
    fun render() {
        UIState.backup()
        // Undo Minecraft's changes

        this.context.resetGLAll()

        val tasksCopy = ArrayList<Runnable>()
        synchronized(commands) {
            tasksCopy.addAll(commands)
            commands.clear()
        }
        tasksCopy.forEach { it.run() }
        tasksCopy.clear()

        // Render

        surface.canvas.clear(Color.TRANSPARENT)
        manager.scene.render(canvas.asComposeCanvas(), System.nanoTime())
        context.flush()

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        UIState.restore()
    }

    // Initialize this once the window is created
    fun initContext() {
        initFbo()
        context = DirectContext.makeGL()
    }

    // Self documenting code
    fun onResize() {
        targetFbo.destroyBuffers()
        initFbo()
        initSkia()
    }

    // Self documenting code v2
    private fun initFbo() {
        targetFbo = TextureTarget(mc.window.width, mc.window.height, false, Minecraft.ON_OSX)
    }

    fun initSkia() {
        // Close the previous surface and render target
        if (this::surface.isInitialized)
            surface.close()

        // Close the previous render target
        if (this::renderTarget.isInitialized)
            renderTarget.close()

        if (this::manager.isInitialized)
            manager.finalizeCompose()

        // Create a new render target and surface
        renderTarget = BackendRenderTarget.makeGL(
            mc.window.width,
            mc.window.height,
            0,
            8,
            Minecraft.getInstance().mainRenderTarget.frameBufferId,
            FramebufferFormat.GR_GL_RGBA8
        )

        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB,
        )!!

        manager = ComposeManager(mc.window.width, mc.window.height) {
            notesWindow()
        }

        canvas = surface.canvas
    }

    override fun execute(command: Runnable) {
        synchronized(commands) {
            commands.add(command)
        }
    }
}