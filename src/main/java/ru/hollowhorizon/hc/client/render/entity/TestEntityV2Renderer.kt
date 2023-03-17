package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3f
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.entities.TestEntityV2
import ru.hollowhorizon.hc.common.registry.ModModels

class TestEntityV2Renderer(entityManager: EntityRendererManager) :
    BTAnimatedEntityRenderer<TestEntityV2>(entityManager, ModModels.BIPED as BTAnimatedModel) {

    override fun getTextureLocation(entity: TestEntityV2) = "hc:models/entity/lololowka.png".rl

    override fun handleEntityOrientation(matrixStackIn: MatrixStack, entity: TestEntityV2, partialTicks: Float) {
        val renderYaw: Float =
            MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot)
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-renderYaw))
    }
}