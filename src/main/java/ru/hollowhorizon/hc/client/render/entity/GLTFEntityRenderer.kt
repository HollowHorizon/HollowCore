package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.LivingRenderer
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.gltf.*
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimation
import ru.hollowhorizon.hc.client.gltf.animation.loadAnimations


class GLTFEntityRenderer<T>(manager: EntityRendererManager) : EntityRenderer<T>(manager),
    IGltfModelReceiver where T : LivingEntity, T : IAnimatedEntity {
    protected var renderedScene: RenderedGltfScene? = null
    protected var animations: List<GLTFAnimation>? = null


    init {
        GlTFModelManager.getInstance().addGltfModelReceiver(this)
    }

    override fun getTextureLocation(entity: T): ResourceLocation {
        return ResourceLocation("hc", "textures/entity/test_entity.png")
    }

    override fun getModelLocation(): ResourceLocation {
        return ResourceLocation("hc", "models/entity/ring.gltf")
    }

    override fun onReceiveSharedModel(renderedModel: RenderedGltfModel) {
        renderedScene = renderedModel.renderedGltfScenes[0]

        animations = renderedModel.loadAnimations()

        HollowCore.LOGGER.info("Received model, animations: ${animations?.joinToString(", ")}")
    }

    fun preRender(entity: T) {
        entity.processAnimations(animations)
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        p_225623_2_: Float,
        particalTick: Float,
        p_225623_4_: MatrixStack,
        p_225623_5_: IRenderTypeBuffer,
        packedLight: Int
    ) {
        preRender(entity)

        val packedOverlay: Int = LivingRenderer.getOverlayCoords(entity, particalTick)

        GL11.glPushMatrix()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_COLOR_MATERIAL)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        )

        GL11.glAlphaFunc(516, 0.1f)

        RenderSystem.multMatrix(p_225623_4_.last().pose())
        GL11.glRotatef(MathHelper.rotLerp(particalTick, entity.yBodyRotO, entity.yBodyRot), 0.0f, 1.0f, 0.0f)

        GL13.glMultiTexCoord2s(
            GL13.GL_TEXTURE2,
            (packedLight and '\uffff'.code).toShort(),
            (packedLight shr 16 and '\uffff'.code).toShort()
        )
        GL13.glMultiTexCoord2s(
            GL13.GL_TEXTURE3,
            (packedOverlay and '\uffff'.code).toShort(),
            (packedOverlay shr 16 and '\uffff'.code).toShort()
        )
        if (GlTFModelManager.getInstance().isShaderModActive) {
            renderedScene!!.renderForShaderMod()
        } else {
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            renderedScene!!.renderForVanilla()
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
        RenderedGltfModel.nodeGlobalTransformLookup.clear()
        GL11.glPopAttrib()
        GL11.glPopMatrix()
        super.render(entity, p_225623_2_, particalTick, p_225623_4_, p_225623_5_, packedLight)
    }
}