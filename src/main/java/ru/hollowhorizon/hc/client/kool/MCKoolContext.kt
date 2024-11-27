package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.KoolContext
import de.fabmax.kool.input.PlatformInputJvm
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.mixins.kool.PlatformInputJvmAccessor
import java.awt.Desktop
import java.net.URI

class MCKoolContext : KoolContext() {

    init {
        KoolHooks.createContext(this)
        isWindowFocused = true
        (PlatformInputJvm as PlatformInputJvmAccessor).apply {
            createCursors()
            PointerInputSetup.setup(Minecraft.getInstance().window.window)
        }
    }

    private var prevFrameTime = 0L
    override val backend = MCRenderBackendGl(this)
    private val window = Minecraft.getInstance().window

    override var isFullscreen: Boolean
        get() = window.isFullscreen
        set(value) {
            if (window.isFullscreen != value) window.toggleFullScreen()
        }
    override val windowHeight: Int get() = window.height
    override val windowWidth: Int get() = window.width

    override fun getSysInfos() = emptyList<String>()

    override fun openUrl(url: String, sameWindow: Boolean) {
        Desktop.getDesktop().browse(URI(url))
    }

    override fun run() {}

    fun renderFrame() {
        KoolHooks.resetShaders(this)
        KoolHooks.executeCoroutineTasks()


        // determine time delta
        val time = System.nanoTime()
        val dt = (time - prevFrameTime) / 1e9
        prevFrameTime = time

        // setup draw queues for all scenes / render passes
        render(dt)

        // execute draw queues
        backend.renderFrame(this)
    }
}