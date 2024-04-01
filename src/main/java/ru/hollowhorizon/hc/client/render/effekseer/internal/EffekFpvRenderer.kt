package ru.hollowhorizon.hc.client.render.effekseer.internal

import net.minecraft.client.player.LocalPlayer

fun interface EffekFpvRenderer {
    fun `hollowcore$renderFpvEffek`(partial: Float, player: LocalPlayer)
}
