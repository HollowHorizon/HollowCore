package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.LivingRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.vector.Matrix4f
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.models.core.materials.IBTMaterial
import ru.hollowhorizon.hc.client.models.core.materials.MaterialResourceManager
import ru.hollowhorizon.hc.client.models.core.model.BTModel
import ru.hollowhorizon.hc.client.render.data.IBTRenderDataContainer
import ru.hollowhorizon.hc.client.render.data.RenderDataManager
import javax.annotation.ParametersAreNonnullByDefault


@OnlyIn(Dist.CLIENT)
abstract class BTEntityRenderer<T : Entity> protected constructor(
    renderManager: EntityRendererManager,
    val model: BTModel,
) : EntityRenderer<T>(renderManager) {
    var modelRenderData: IBTRenderDataContainer? = null
    var activeEntities = HashMap<T, Boolean>()

    init {
        setupModelRenderData(model)
    }

    open fun setupModelRenderData(model: BTModel) {
        modelRenderData = RenderDataManager.MANAGER.getRenderDataForModel(model, true)
    }

    fun getRenderType(entityType: T, isVisible: Boolean, visibleToPlayer: Boolean): RenderType? {
        val resourcelocation = this.getTextureLocation(entityType)
        return if (visibleToPlayer) {
            RenderType.entityTranslucent(resourcelocation)
        } else if (isVisible) {
            RenderType.entityCutoutNoCull(resourcelocation)
        } else {
            if (entityType.isGlowing) RenderType.outline(resourcelocation) else null
        }
    }

    fun isVisible(livingEntityIn: T): Boolean {
        return !livingEntityIn.isInvisible
    }

    open fun drawModel(
        renderType: RenderType, entityIn: T, entityYaw: Float, partialTicks: Float,
        matrixStackIn: MatrixStack, projectionMatrix: Matrix4f, packedLightIn: Int,
        packedOverlay: Int, program: IBTMaterial, buffer: IRenderTypeBuffer,
    ) {
        program.initRender(renderType, matrixStackIn, projectionMatrix, packedLightIn, packedOverlay)
        modelRenderData?.render()
        program.endRender(renderType)
    }

    private fun initializeRender() {
        modelRenderData?.upload()
    }

    @ParametersAreNonnullByDefault
    override fun render(
        entityIn: T,
        entityYaw: Float,
        partialTicks: Float,
        matrixStackIn: MatrixStack,
        bufferIn: IRenderTypeBuffer,
        packedLightIn: Int,
    ) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn)
        val visible = isVisible(entityIn)
        val visibleToPlayer = !visible && !entityIn.isInvisibleTo(Minecraft.getInstance().player!!)
        val rendertype: RenderType = getRenderType(entityIn, visible, visibleToPlayer) ?: return
        bufferIn.getBuffer(rendertype)
        val program = MaterialResourceManager.INSTANCE.getShaderProgram(model.programName)
        if (!modelRenderData?.isInitialized!!) {
            initializeRender()
        }
        val gameRenderer = Minecraft.getInstance().gameRenderer
        val projMatrix: Matrix4f = gameRenderer.getProjectionMatrix(gameRenderer.mainCamera, partialTicks, true)
        val packedOverlay = if (entityIn is LivingEntity) {
            LivingRenderer.getOverlayCoords(entityIn as LivingEntity, partialTicks)
        } else {
            OverlayTexture.NO_OVERLAY
        }
        drawModel(
            rendertype, entityIn,
            entityYaw, partialTicks, matrixStackIn,
            projMatrix, packedLightIn,
            packedOverlay, program, bufferIn
        )
    }
}