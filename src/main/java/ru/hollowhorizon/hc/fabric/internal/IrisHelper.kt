package ru.hollowhorizon.hc.fabric.internal

//? if fabric && >=1.20.1 {

/*import net.irisshaders.iris.Iris
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline

object IrisHelper {
    @JvmStatic
    fun shouldOverrideShaders() =
        (Iris.getPipelineManager().pipelineNullable as? ShaderRenderingPipeline)?.shouldOverrideShaders() == true
}
*///?} elif fabric && >=1.19.2 {
/*import net.coderbot.iris.Iris
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline

object IrisHelper {
    @JvmStatic
    fun shouldOverrideShaders() =
        (Iris.getPipelineManager().pipelineNullable as? NewWorldRenderingPipeline)?.shouldOverrideShaders() == true
}
*///?}