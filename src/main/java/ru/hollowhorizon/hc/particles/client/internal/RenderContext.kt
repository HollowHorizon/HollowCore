package ru.hollowhorizon.hc.particles.client.internal

import net.minecraftforge.fml.ModList

object RenderContext {
    private val IRIS_MODE = ModList.get().isLoaded("oculus")

    @JvmStatic
    fun renderLevelDeferred() = !IRIS_MODE

    @JvmStatic
    fun renderHandDeferred() = !IRIS_MODE || (isIrisShaderEnabled)

    @JvmStatic
    fun captureHandDepth() = !IRIS_MODE || !isIrisShaderEnabled

    private val isIrisShaderEnabled get() = IRIS_MODE && IrisProxy.isIrisShaderEnabled
}
