package ru.hollowhorizon.hc.internal

import net.irisshaders.iris.Iris
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline

object IrisHelper {
    @JvmStatic
    fun shouldOverrideShaders() =
        (Iris.getPipelineManager().pipelineNullable as? ShaderRenderingPipeline)?.shouldOverrideShaders() == true
}