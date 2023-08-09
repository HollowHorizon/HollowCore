package ru.hollowhorizon.hc.client.utils.math

import net.minecraft.util.Mth

object Interpolation {

    fun easeInSine(x: Float): Float {
        return 1 - Mth.cos((x * Math.PI.toFloat()) / 2F)
    }

    fun easeOutSine(x: Float): Float {
        return Mth.sin((x * Math.PI.toFloat()) / 2F)
    }
}