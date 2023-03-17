package ru.hollowhorizon.hc.client.render.entity.layers

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.entity.Entity
import net.minecraft.util.math.vector.Matrix4f
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.materials.IBTMaterial
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

interface IBTAnimatedLayerRenderer<T> where T : Entity, T : IBTAnimatedEntity<T> {
    fun render(
        matrixStack: MatrixStack, renderBuffer: IRenderTypeBuffer, packedLight: Int, entityIn: T, pose: IPose,
        partialTicks: Float, ageInTicks: Float, currentMaterial: IBTMaterial, projectionMatrix: Matrix4f,
    )
}