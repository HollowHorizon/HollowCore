package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.KoolContext
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.configJvm
import de.fabmax.kool.pipeline.backend.BackendFeatures
import de.fabmax.kool.pipeline.backend.DeviceCoordinates
import de.fabmax.kool.pipeline.backend.gl.GlslGenerator
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl
import de.fabmax.kool.pipeline.backend.gl.TimeQuery

class MCRenderBackendGl(ctx: KoolContext) : RenderBackendGl(KoolSystem.configJvm.msaaSamples, MCGlApi, ctx) {
    override val features: BackendFeatures

    init {
        MCGlApi.initOpenGl(this)
        KoolHooks.setupScene(sceneRenderer)
        features = BackendFeatures(
            computeShaders = true,
            cubeMapArrays = true,
            reversedDepth = MCGlApi.capabilities.hasClipControl
        )
        deviceCoordinates = DeviceCoordinates.OPEN_GL_ZERO_TO_ONE
    }

    override var frameGpuTime: Double = 0.0
    private val timer = TimeQuery(MCGlApi)

    override val glslGeneratorHints: GlslGenerator.Hints
        get() = GlslGenerator.Hints("#version 330 core")

    override fun cleanup(ctx: KoolContext) {}

    override fun renderFrame(ctx: KoolContext) {
        if (timer.isAvailable) {
            frameGpuTime = timer.getQueryResultMillis()
        }

        timer.timedScope {
            super.renderFrame(ctx)
        }
    }

}