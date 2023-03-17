package ru.hollowhorizon.hc.client.render.data

import net.minecraftforge.api.distmarker.Dist

import net.minecraftforge.api.distmarker.OnlyIn


@OnlyIn(Dist.CLIENT)
interface IBTRenderDataContainer : IBTRenderData {
    val isInitialized: Boolean

    fun incrementFrameCount()
    val frameSinceLastRender: Int
}