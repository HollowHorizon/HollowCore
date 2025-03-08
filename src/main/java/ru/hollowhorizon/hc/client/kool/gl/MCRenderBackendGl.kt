package ru.hollowhorizon.hc.client.kool.gl

import de.fabmax.kool.KoolContext
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.configJvm
import de.fabmax.kool.pipeline.backend.BackendFeatures
import de.fabmax.kool.pipeline.backend.DeviceCoordinates
import de.fabmax.kool.pipeline.backend.gl.GlslGenerator
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl
import de.fabmax.kool.pipeline.backend.gl.TimeQuery
import de.fabmax.kool.pipeline.backend.stats.BackendStats
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.Time
import de.fabmax.kool.util.Viewport
import ru.hollowhorizon.hc.client.kool.awaitedStorageBuffers
import ru.hollowhorizon.hc.client.kool.drawOffscreen
import ru.hollowhorizon.hc.client.kool.readbackStorageBuffers
import ru.hollowhorizon.hc.client.kool.sortedOffscreenPasses

class MCRenderBackendGl(ctx: KoolContext) : RenderBackendGl(KoolSystem.configJvm.msaaSamples, MCGlApi, ctx) {
    val gl = MCGlApi
    override val features: BackendFeatures
    val mcSceneRenderer = MCSceneRenderPass(numSamples, this)

    init {
        gl.initOpenGl(this)
        mcSceneRenderer.resolveDirect = true
        features = BackendFeatures(
            computeShaders = true,
            cubeMapArrays = true,
            reversedDepth = gl.capabilities.hasClipControl
        )
        deviceCoordinates = DeviceCoordinates.OPEN_GL
    }

    override var frameGpuTime: Double = 0.0
    private val timer = TimeQuery(gl)

    override val glslGeneratorHints: GlslGenerator.Hints
        get() = GlslGenerator.Hints("#version 430 core")

    override fun cleanup(ctx: KoolContext) {}

    override fun renderFrame(ctx: KoolContext) {
        if (timer.isAvailable) {
            frameGpuTime = timer.getQueryResultMillis()
        }

        timer.timedScope {
            renderMCFrame(ctx)
        }
    }

    private val windowViewport = Viewport(0, 0, 0, 0)

    fun renderMCFrame(ctx: KoolContext) {
        BackendStats.resetPerFrameCounts()

        getWindowViewport(windowViewport)
        mcSceneRenderer.applySize(windowViewport.width, windowViewport.height)

        doOffscreenPasses(ctx.backgroundScene)

        for (i in ctx.scenes.indices) {
            val scene = ctx.scenes[i]
            if (scene.isVisible) {
                val t = Time.precisionTime
                doOffscreenPasses(scene)
                mcSceneRenderer.draw(scene)
                scene.sceneDrawTime = Time.precisionTime - t
            }
        }

        if (useFloatDepthBuffer) {
            mcSceneRenderer.resolve(gl.DEFAULT_FRAMEBUFFER, gl.COLOR_BUFFER_BIT)
        }

        if (awaitedStorageBuffers.isNotEmpty()) {
            readbackStorageBuffers()
        }
    }

    private fun doOffscreenPasses(scene: Scene) {
        for (i in scene.sortedOffscreenPasses.indices) {
            val pass = scene.sortedOffscreenPasses[i]
            if (pass.isEnabled) {
                drawOffscreen(pass)
                pass.afterDraw()
            }
        }
    }
}