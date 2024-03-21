package ru.hollowhorizon.hc.particles.client.internal

import net.irisshaders.iris.api.v0.IrisApi

internal object IrisProxy {
    @JvmStatic
    val isIrisShaderEnabled get() = IrisApi.getInstance().isShaderPackInUse
}