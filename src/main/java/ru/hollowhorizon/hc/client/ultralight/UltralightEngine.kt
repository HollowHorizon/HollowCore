package ru.hollowhorizon.hc.client.ultralight

import com.labymedia.ultralight.UltralightJava
import com.labymedia.ultralight.UltralightPlatform
import com.labymedia.ultralight.UltralightRenderer
import com.labymedia.ultralight.config.FontHinting
import com.labymedia.ultralight.config.UltralightConfig
import com.labymedia.ultralight.gpu.UltralightGPUDriverNativeUtil
import com.labymedia.ultralight.gpu.UltralightOpenGLGPUDriverNative
import com.labymedia.ultralight.os.OperatingSystem
import com.labymedia.ultralight.plugin.logging.UltralightLogLevel
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.Screen
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.ultralight.impl.BrowserFileSystem
import ru.hollowhorizon.hc.client.ultralight.impl.glfw.GlfwClipboardAdapter
import ru.hollowhorizon.hc.client.ultralight.impl.glfw.GlfwCursorAdapter
import ru.hollowhorizon.hc.client.ultralight.impl.glfw.GlfwInputAdapter
import ru.hollowhorizon.hc.client.ultralight.impl.renderer.CpuViewRenderer
import ru.hollowhorizon.hc.client.utils.mc

object UltralightEngine {

    private var driver: UltralightOpenGLGPUDriverNative? = null
    val window = mc.window.window
    var platform = ThreadLock<UltralightPlatform>()
    var renderer = ThreadLock<UltralightRenderer>()

    lateinit var clipboardAdapter: GlfwClipboardAdapter
    lateinit var cursorAdapter: GlfwCursorAdapter
    lateinit var inputAdapter: GlfwInputAdapter

    val inputAwareOverlay: ViewOverlay?
        get() = viewOverlays.find { it is ScreenViewOverlay && mc.screen == it.screen && it.state == ViewOverlayState.VISIBLE }
    private val viewOverlays = mutableListOf<ViewOverlay>()

    val resources = UltralightResources()

    /**
     * Frame limiter
     */
    private var isInitialized = false
    private const val MAX_FRAME_RATE = 60
    private var lastRenderTime = 0.0

    /**
     * Initializes the platform
     */
    fun init() {
        if (isInitialized) return
        isInitialized = true

        HollowCore.LOGGER.info("Loading ultralight...")
        initNatives()

        // Setup platform
        HollowCore.LOGGER.debug("Setting up ultralight platform")
        platform.lock(UltralightPlatform.instance())
        platform.get().setConfig(
            UltralightConfig()
                .animationTimerDelay(1.0 / MAX_FRAME_RATE)
                .scrollTimerDelay(1.0 / MAX_FRAME_RATE)
                .cachePath(resources.cacheRoot.absolutePath)
                .fontHinting(FontHinting.SMOOTH)
                .forceRepaint(true)
        )

        //this.driver = UltralightOpenGLGPUDriverNative(window, false, GLFW.Functions.GetProcAddress)
        //platform.get().setGPUDriver(this.driver!!)
        platform.get().usePlatformFontLoader()
        platform.get().setFileSystem(BrowserFileSystem())
        platform.get().setLogger { level, message ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (level) {
                UltralightLogLevel.ERROR -> HollowCore.LOGGER.error("[Ul] $message")
                UltralightLogLevel.WARNING -> HollowCore.LOGGER.warn("[Ul] $message")
                UltralightLogLevel.INFO -> HollowCore.LOGGER.info("[Ul] $message")
            }
        }

        platform.get().setClipboard(GlfwClipboardAdapter())

        // Setup renderer
        HollowCore.LOGGER.info("Setting up ultralight renderer")

        val ulRenderer = UltralightRenderer.create()
        ulRenderer.logMemoryUsage()
        renderer.lock(ulRenderer)

        // Setup GLFW adapters
        clipboardAdapter = GlfwClipboardAdapter()
        cursorAdapter = GlfwCursorAdapter()
        inputAdapter = GlfwInputAdapter()

        HollowCore.LOGGER.info("Successfully loaded ultralight!")
    }

    /**
     * Initializes the natives, this is required for ultralight to work.
     *
     * This will download the required natives and resources and load them.
     */
    private fun initNatives() {
        // Check resources
        HollowCore.LOGGER.info("Checking resources...")
        resources.downloadResources()

        // Load natives from native directory inside root folder
        HollowCore.LOGGER.info("Loading ultralight natives")
        val natives = resources.binRoot.toPath()
        HollowCore.LOGGER.info("Native path: $natives")

        val libs = listOf(
            "glib-2.0-0",
            "gobject-2.0-0",
            "gmodule-2.0-0",
            "gio-2.0-0",
            "gstreamer-full-1.0",
            "gthread-2.0-0"
        )
        HollowCore.LOGGER.debug("Libraries: $libs")

        val os = OperatingSystem.get()
        for (lib in libs) {
            HollowCore.LOGGER.debug("Loading library $lib")
            System.load(natives.resolve(os.mapLibraryName(lib)).toAbsolutePath().toString())
        }

        HollowCore.LOGGER.debug("Loading UltralightJava")
        UltralightJava.load(natives)
        HollowCore.LOGGER.debug("Loading UltralightGPUDriver")
        UltralightGPUDriverNativeUtil.load(natives)
    }

    fun shutdown() {
        cursorAdapter.cleanup()
    }

    fun update() {
        viewOverlays
            .forEach(ViewOverlay::update)
        renderer.get().update()
    }

    fun render(layer: RenderLayer, stack: MatrixStack) {
        frameLimitedRender()

        viewOverlays
            .filter { it.layer == layer && it.state != ViewOverlayState.HIDDEN }
            .forEach {
                it.render(stack)
            }
    }

    private fun frameLimitedRender() {
        val frameTime = 1.0 / MAX_FRAME_RATE
        val time = System.nanoTime() / 1e9
        val delta = time - lastRenderTime

        if (delta < frameTime) {
            return
        }

        renderer.get().render()
        lastRenderTime = time
    }

    fun resize(width: Long, height: Long) {
        viewOverlays.forEach { it.resize(width, height) }
    }

    fun newSplashView() =
        ViewOverlay(RenderLayer.SPLASH_LAYER, newViewRenderer()).also { viewOverlays += it }

    fun newOverlayView() =
        ViewOverlay(RenderLayer.OVERLAY_LAYER, newViewRenderer()).also { viewOverlays += it }

    fun newScreenView(screen: Screen, adaptedScreen: Screen? = null, parentScreen: Screen? = null) =
        ScreenViewOverlay(newViewRenderer(), screen, adaptedScreen, parentScreen).also { viewOverlays += it }

    /**
     * Removes the view overlay from the list of overlays
     */
    fun removeView(viewOverlay: ViewOverlay) {
        viewOverlay.setOnStageChange {
            if (it == ViewOverlayState.END) {
                viewOverlay.free()
                viewOverlays.remove(viewOverlay)
            }
        }
    }

    /**
     * Creates a new view renderer
     */
    private fun newViewRenderer() = CpuViewRenderer()

}

enum class RenderLayer {
    OVERLAY_LAYER, SCREEN_LAYER, SPLASH_LAYER
}

class ThreadLock<T> {

    private lateinit var lockedOnThread: Thread
    private var content: T? = null

    fun get(): T {
        if (Thread.currentThread() != lockedOnThread) {
            error("thread-locked content accessed by other thread current(${Thread.currentThread().name}) required(${lockedOnThread})")
        }

        return content!!
    }

    fun lock(t: T) {
        lockedOnThread = Thread.currentThread()
        content = t
    }

}