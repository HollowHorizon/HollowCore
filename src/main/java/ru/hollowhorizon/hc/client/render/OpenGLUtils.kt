package ru.hollowhorizon.hc.client.render

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3d

object OpenGLUtils {
    fun drawLine(
        bufferbuilder: BufferBuilder, matrix: Matrix4f,
        from: Vector3d, to: Vector3d,
        r: Float, g: Float, b: Float, a: Float,
    ) {
        bufferbuilder
            .vertex(matrix, from.x.toFloat(), from.y.toFloat() - 0.1f, from.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
        bufferbuilder
            .vertex(matrix, to.x.toFloat(), to.y.toFloat() - 0.1f, to.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
    }
}

