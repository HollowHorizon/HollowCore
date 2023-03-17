package ru.hollowhorizon.hc.client.render.data

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
interface IBTRenderData {
    fun render()
    fun cleanup()
    fun upload()
}