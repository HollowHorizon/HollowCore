package ru.hollowhorizon.hc.client.utils.math

import net.minecraft.util.math.MathHelper

object Interpolation {
    fun simple(x: Float): Float {
        return x
    }

    fun easeInSine(x: Float): Float {
        return 1 - MathHelper.cos((x * Math.PI.toFloat()) / 2F)
    }

    fun easeOutSine(x: Float): Float {
        return MathHelper.sin((x * Math.PI.toFloat()) / 2F)
    }
}