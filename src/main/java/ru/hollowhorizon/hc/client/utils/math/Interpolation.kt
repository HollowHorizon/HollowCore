package ru.hollowhorizon.hc.client.utils.math

import net.minecraft.util.Mth
import kotlin.math.pow

const val EL = (2 * Mth.PI) / 3
const val EL2 = (2 * Mth.PI) / 4.5f
const val n1 = 7.5625f
const val d1 = 2.75f

enum class Interpolation(val function: (Float) -> Float) {
    LINEAR({ x -> x }),
    SINE_IN({ x -> 1.0f - Mth.cos((x * Mth.PI) / 2) }),
    SINE_OUT({ x -> Mth.sin((Mth.PI * x) / 2) }),
    SINE_IN_OUT({ x -> 0.5f * (1.0f - Mth.cos((Mth.PI * x) / 2)) }),
    QUAD_IN({ x -> x * x }),
    QUAD_OUT({ x -> 1f - (1 - x) * (1 - x) }),
    QUAD_IN_OUT({ x -> if (x < 0.5f) 2 * x * x else 1 - (-2 * x + 2) * (-2 * x + 2) / 2 }),
    CUBIC_IN({ x -> x * x * x }),
    CUBIC_OUT({ x -> 1f - (1 - x) * (1 - x) * (1 - x) }),
    CUBIC_IN_OUT({ x -> if (x < 0.5f) 4 * x * x * x else 1 - (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) / 2 }),
    QUART_IN({ x -> x * x * x * x }),
    QUART_OUT({ x -> 1f - (1 - x) * (1 - x) * (1 - x) * (1 - x) }),
    QUART_IN_OUT({ x -> if (x < 0.5f) 8 * x * x * x * x else 1 - (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) / 2 }),
    QUINT_IN({ x -> x * x * x * x * x }),
    QUINT_OUT({ x -> 1f - (1 - x) * (1 - x) * (1 - x) * (1 - x) * (1 - x) }),
    QUINT_IN_OUT({ x -> if (x < 0.5f) 16 * x * x * x * x * x else 1 - (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) / 2 }),
    EXPO_IN({ x -> if (x == 0f) 0f else 2f.pow(10 * x - 10) }),
    EXPO_OUT({ x -> if (x == 1f) 1f else 1f - 2f.pow(-10 * x) }),
    EXPO_IN_OUT({ x ->
        when (x) {
            0f -> 0f
            1f -> 1f
            in 0f..0.5f -> 2f.pow(20f * x - 10f) / 2
            else -> (2f - 2f.pow(-20f * x + 10f)) / 2
        }
    }),
    CIRC_IN({ x -> 1f - Mth.sqrt(1 - x * x) }),
    CIRC_OUT({ x -> Mth.sqrt(1 - (x - 1).pow(2)) }),
    CIRC_IN_OUT({ x -> if (x < 0.5f) (1 - Mth.sqrt(1 - (2 * x).pow(2))) / 2 else Mth.sqrt(1 - (-2 * x + 2).pow(2)) / 2 }),
    BACK_IN({ x -> 2.70158f * x.pow(3) - 1.70158f * x.pow(2) }),
    BACK_OUT({ x -> 1 + 2.70158f * (x - 1).pow(3) + 1.70158f * (x - 1).pow(2) }),
    BACK_IN_OUT({ x ->
        if (x < 0.5f) ((2 * x).pow(2) * (8.45316f * x - 3.22658f)) / 2
        else ((2 * x - 2).pow(2) * (4.22658f * (x * 2 - 2) + 3.22658f) + 2) / 2
    }),
    ELASTIC_IN({ x ->
        when (x) {
            0f -> 0f
            1f -> 1f
            else -> -(2f.pow(10 * x - 10)) * Mth.sin((x * 10 - 10.75f) * EL)
        }
    }),
    ELASTIC_OUT({ x ->
        when (x) {
            0f -> 0f
            1f -> 1f
            else -> 2f.pow(-10 * x) * Mth.sin((x * 10 - 0.75f) * EL) + 1
        }
    }),
    ELASTIC_IN_OUT({ x ->
        when (x) {
            0f -> 0f
            1f -> 1f
            in 0f..0.5f -> -(2f.pow(20 * x - 10) * Mth.sin((20 * x - 11.125f) * EL2)) / 2f
            else -> (2f.pow(-20 * x + 10) * Mth.sin((20 * x - 11.125f) * EL2)) / 2f + 1
        }
    }),
    BOUNCE_IN({ xo ->
        val x = 1 - xo

        when {
            x < 1 / d1 -> 1 - n1 * x * x
            x < 2 / d1 -> 1- n1 * (x - 1.5f / d1) * (x - 1.5f) + 0.75f
            x < 2.5 / d1 -> 1 - n1 * (x - 2.25f / d1) * (x - 2.25f) + 0.9375f
            else -> 1 - n1 * (x - 2.625f / d1) * (x - 2.625f) + 0.984375f
        }
    }),
    BOUNCE_OUT({ x ->
        when {
            x < 1 / d1 -> n1 * x * x
            x < 2 / d1 -> n1 * (x - 1.5f / d1) * (x - 1.5f) + 0.75f
            x < 2.5 / d1 -> n1 * (x - 2.25f / d1) * (x - 2.25f) + 0.9375f
            else -> n1 * (x - 2.625f / d1) * (x - 2.625f) + 0.984375f
        }
    }),
    BOUNCE_IN_OUT({ xo ->
        if(xo < 0.5f) {
            val x = 1 - xo * 2
            when {
                x < 1 / d1 -> (1 - n1 * x * x) / 2
                x < 2 / d1 -> (1 - n1 * (x - 1.5f / d1) * (x - 1.5f) + 0.75f) / 2
                x < 2.5 / d1 -> (1 - n1 * (x - 2.25f / d1) * (x - 2.25f) + 0.9375f) / 2
                else -> (1 - n1 * (x - 2.625f / d1) * (x - 2.625f) + 0.984375f) / 2
            }
        } else {
            val x = xo * 2 - 1
            when {
                x < 1 / d1 -> (1 + n1 * x * x) / 2
                x < 2 / d1 -> (1 + n1 * (x - 1.5f / d1) * (x - 1.5f) + 0.75f) / 2
                x < 2.5 / d1 -> (1 + n1 * (x - 2.25f / d1) * (x - 2.25f) + 0.9375f) / 2
                else -> (1 + n1 * (x - 2.625f / d1) * (x - 2.625f) + 0.984375f) / 2
            }
        }
    });
}