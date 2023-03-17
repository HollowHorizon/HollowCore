package ru.hollowhorizon.hc.client.models.core

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.util.Util
import net.minecraft.util.math.vector.Vector3f

object GlobalRenderInfo {
    @JvmField
    val DIFFUSE_LIGHT_0: Vector3f = Util.make(
        Vector3f(
            0.2f, 1.0f, -0.7f
        ), Vector3f::normalize
    )
    @JvmField
    val DIFFUSE_LIGHT_1: Vector3f = Util.make(
        Vector3f(
            -0.2f, 1.0f, 0.7f
        ), Vector3f::normalize
    )

    @JvmField
    var currentFrameGlobal = MatrixStack()
}