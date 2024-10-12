package ru.hollowhorizon.hc.fabric.internal

//? if fabric {

import net.irisshaders.iris.Iris
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline

object IrisHelper {
    @JvmStatic
    fun shouldOverrideShaders() =
        (Iris.getPipelineManager().pipelineNullable as? ShaderRenderingPipeline)?.shouldOverrideShaders() == true
}
//?}