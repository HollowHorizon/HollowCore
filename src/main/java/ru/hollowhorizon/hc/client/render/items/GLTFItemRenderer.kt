package ru.hollowhorizon.hc.client.render.items

import com.modularmods.mcgltf.MCglTF
import com.modularmods.mcgltf.RenderedGltfModel
import com.modularmods.mcgltf.RenderedGltfScene
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Matrix3f
import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.extensions.common.IClientItemExtensions
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.utils.rl


class GLTFItemRenderer: IClientItemExtensions {
    val model = ExampleItemRenderer()
    override fun getCustomRenderer(): BlockEntityWithoutLevelRenderer {
        return object : BlockEntityWithoutLevelRenderer(
            Minecraft.getInstance().blockEntityRenderDispatcher,
            Minecraft.getInstance().entityModels
        ) {
            override fun renderByItem(
                pStack: ItemStack,
                pTransformType: TransformType,
                pPoseStack: PoseStack,
                pBuffer: MultiBufferSource,
                pPackedLight: Int,
                pPackedOverlay: Int
            ) {
                model.renderByItem(pTransformType, pPoseStack, pBuffer, pPackedLight, pPackedOverlay)
            }
        }
    }
}


class ExampleItemRenderer {
    protected var renderedScene: RenderedGltfScene? = null

    fun renderByItem(
        p_108831_: TransformType?,
        stack: PoseStack,
        p_108833_: MultiBufferSource?,
        p_108834_: Int,
        packedLight: Int,
    ) {
        val mc = Minecraft.getInstance()
        val currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)
        val currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)
        val currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING)
        val currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE)
        val currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        when (p_108831_) {
            TransformType.THIRD_PERSON_LEFT_HAND, TransformType.THIRD_PERSON_RIGHT_HAND, TransformType.HEAD -> {
                val currentBlend = GL11.glGetBoolean(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_BLEND)
                GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                //RenderedGltfModel.CURRENT_POSE = stack.last().pose()
                //RenderedGltfModel.CURRENT_NORMAL = stack.last().normal()
                GL30.glVertexAttribI2i(
                    RenderedGltfModel.vaUV1,
                    packedLight and '\uffff'.code,
                    packedLight shr 16 and '\uffff'.code
                )
                GL30.glVertexAttribI2i(
                    RenderedGltfModel.vaUV2,
                    p_108834_ and '\uffff'.code,
                    p_108834_ shr 16 and '\uffff'.code
                )
                if (MCglTF.getInstance().isShaderModActive) {
                    //renderedScene!!.renderOptiOculus()
                } else {
                    GL13.glActiveTexture(GL13.GL_TEXTURE2)
                    val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)
                    GL13.glActiveTexture(GL13.GL_TEXTURE1)
                    val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    mc.gameRenderer.overlayTexture().setupOverlayColor()
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1))
                    mc.gameRenderer.overlayTexture().teardownOverlayColor()
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                    val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    //renderedScene!!.renderVanilla()
                    GL13.glActiveTexture(GL13.GL_TEXTURE2)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
                    GL13.glActiveTexture(GL13.GL_TEXTURE1)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
                }
                GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0)
                GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0)
                if (!currentBlend) GL11.glDisable(GL11.GL_BLEND)
            }

            TransformType.FIRST_PERSON_LEFT_HAND, TransformType.FIRST_PERSON_RIGHT_HAND, TransformType.GROUND, TransformType.FIXED -> {
                val currentBlend = GL11.glGetBoolean(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_BLEND)
                GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                //RenderedGltfModel.CURRENT_POSE = stack.last().pose()
                //RenderedGltfModel.CURRENT_NORMAL = stack.last().normal()
                GL30.glVertexAttribI2i(
                    RenderedGltfModel.vaUV2,
                    p_108834_ and '\uffff'.code,
                    p_108834_ shr 16 and '\uffff'.code
                )
                if (MCglTF.getInstance().isShaderModActive) {
                    //renderedScene!!.renderOptiOculus()
                } else {
                    GL13.glActiveTexture(GL13.GL_TEXTURE2)
                    val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)
                    GL13.glActiveTexture(GL13.GL_TEXTURE1)
                    val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                    val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                    //renderedScene!!.renderVanilla()
                    GL13.glActiveTexture(GL13.GL_TEXTURE2)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
                    GL13.glActiveTexture(GL13.GL_TEXTURE1)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
                }
                GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0)
                if (!currentBlend) GL11.glDisable(GL11.GL_BLEND)
            }

            TransformType.GUI -> {
                val rotateAround = Quaternion(0.0f, 1.0f, 0.0f, 0.0f)
                //RenderedGltfModel.CURRENT_POSE = Matrix4f(RenderSystem.getModelViewMatrix()).apply { this.multiply(rotateAround) }
                //RenderedGltfModel.CURRENT_NORMAL = Matrix3f().apply {this.mul(rotateAround)}
                GL13.glActiveTexture(GL13.GL_TEXTURE2)
                val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().defaultColorMap)
                GL13.glActiveTexture(GL13.GL_TEXTURE1)
                val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                //renderedScene!!.renderVanilla()
                GL13.glActiveTexture(GL13.GL_TEXTURE2)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
                GL13.glActiveTexture(GL13.GL_TEXTURE1)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
            }

            else -> {}
        }
        if (!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST)
        if (currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE) else GL11.glDisable(GL11.GL_CULL_FACE)
        GL30.glBindVertexArray(currentVAO)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
    }
}