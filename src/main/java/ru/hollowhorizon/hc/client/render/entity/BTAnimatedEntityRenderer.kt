package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.potion.Effects
import net.minecraft.util.math.vector.Matrix4f
import org.lwjgl.opengl.GL11
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.client.models.core.applyTransformToStack
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFSkeleton
import ru.hollowhorizon.hc.client.models.core.materials.IBTMaterial
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel
import ru.hollowhorizon.hc.client.models.core.model.BTModel
import ru.hollowhorizon.hc.client.render.data.RenderDataManager
import ru.hollowhorizon.hc.client.render.entity.layers.IBTAnimatedLayerRenderer
import ru.hollowhorizon.hc.client.utils.math.Matrix4d
import ru.hollowhorizon.hc.client.utils.use
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import javax.annotation.ParametersAreNonnullByDefault


abstract class BTAnimatedEntityRenderer<T> protected constructor(
    renderManager: EntityRendererManager,
    animatedModel: BTAnimatedModel,
) :
    BTEntityRenderer<T>(renderManager, animatedModel) where T : Entity, T : IBTAnimatedEntity<T> {
    val renderLayers = ArrayList<IBTAnimatedLayerRenderer<T>>()

    override fun setupModelRenderData(model: BTModel) {
        modelRenderData = RenderDataManager.MANAGER.getAnimatedRenderDataForModel(
            (model as BTAnimatedModel), true
        )
    }

    fun addRenderLayer(layer: IBTAnimatedLayerRenderer<T>) {
        renderLayers.add(layer)
    }

    open fun handleEntityOrientation(matrixStackIn: MatrixStack, entity: T, partialTicks: Float) {}

    fun moveMatrixStackToBone(entityIn: T, boneName: String, matrixStack: MatrixStack, pose: IPose) {
        val skeleton: BoneMFSkeleton = entityIn.skeleton

        val boneId = skeleton.getBoneId(boneName)
        if (boneId != -1) {
            val boneMat = pose.getJointMatrix(boneId)
            applyTransformToStack(boneMat, matrixStack)
        }

    }

    protected fun getTimeAlive(entity: T, partialTicks: Float): Float {
        return entity.tickCount + partialTicks
    }

    @ParametersAreNonnullByDefault
    override fun drawModel(
        renderType: RenderType, entityIn: T, entityYaw: Float,
        partialTicks: Float, matrixStackIn: MatrixStack,
        projectionMatrix: Matrix4f, packedLightIn: Int,
        packedOverlay: Int, program: IBTMaterial, buffer: IRenderTypeBuffer,
    ) {
        matrixStackIn.use {
            handleEntityOrientation(matrixStackIn, entityIn, partialTicks)
            program.initRender(renderType, matrixStackIn, projectionMatrix, packedLightIn, packedOverlay)
            val pose: IPose = entityIn.animationComponent.getCurrentPose(partialTicks)
            val skeleton: BoneMFSkeleton = entityIn.skeleton



            // we need to upload this every render because if multiple models are sharing one shader
            // the uniforms will be invalid for bind pose, we could consider caching last model rendered
            // with a particular program and skip it
            program.uploadInverseBindPose(skeleton.inverseBindPose)

            program.uploadAnimationFrame(pose)
            modelRenderData!!.render()
            program.endRender(renderType)
            renderLayers.forEach { layer ->
                layer.render(
                    matrixStackIn, buffer, packedLightIn, entityIn, pose, partialTicks,
                    getTimeAlive(entityIn, partialTicks), program, projectionMatrix
                )
            }

        }
    }
}