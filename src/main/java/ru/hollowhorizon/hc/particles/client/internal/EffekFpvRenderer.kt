package ru.hollowhorizon.hc.particles.client.internal

import net.minecraft.client.player.LocalPlayer

fun interface EffekFpvRenderer {
    fun `hollowcore$renderFpvEffek`(partial: Float, player: LocalPlayer)
}
