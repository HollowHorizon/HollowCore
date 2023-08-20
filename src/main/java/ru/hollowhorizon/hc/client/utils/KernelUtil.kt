package ru.hollowhorizon.hc.client.utils

import org.lwjgl.BufferUtils
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.exp

object KernelUtil {
    val loadKernel = KernelUtil::calculateKernel.memoize()

    private fun calculateKernel(radius: Int): FloatBuffer {
        val buffer = BufferUtils.createFloatBuffer(radius)
        val kernel = FloatArray(radius)
        val sigma = radius / 2.0f
        var total = 0.0f
        for (i in 0 until radius) {
            val multiplier = i / sigma
            kernel[i] = 1.0f / (abs(sigma) * 2.5066283f) * exp(-0.5 * multiplier * multiplier).toFloat()
            total += if (i > 0) kernel[i] * 2 else kernel[0]
        }
        for (i in 0 until radius) {
            kernel[i] /= total
        }
        buffer.put(kernel)
        buffer.flip()
        return buffer
    }
}

fun <A, B> ((A) -> B).memoize(): (A) -> B {
    val cache: MutableMap<A, B> = HashMap()
    return {
        cache.getOrPut(it) { this(it) }
    }
}