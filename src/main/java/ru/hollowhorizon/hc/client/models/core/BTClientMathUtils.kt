package ru.hollowhorizon.hc.client.models.core

import com.mojang.blaze3d.matrix.MatrixStack
import ru.hollowhorizon.hc.client.utils.math.Matrix4d

fun applyTransformToStack(inMat: Matrix4d, inStack: MatrixStack) {
    val translation = inMat.translation
    val quat = inMat.rotation
    val scale = inMat.scale

    inStack.translate(translation.x(), translation.y(), translation.z())
    inStack.mulPose(quat)
    inStack.scale(scale.x(), scale.y(), scale.z())
}