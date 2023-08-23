package ru.hollowhorizon.hc.client.render.entity

import com.modularmods.mcgltf.MCglTF
import com.modularmods.mcgltf.RenderedGltfModel
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.gltf.animations.GLTFAnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.HeadLayer
import ru.hollowhorizon.hc.client.gltf.animations.manager.ClientModelManager
import kotlin.jvm.optionals.getOrNull


class GLTFEntityRenderer<T>(manager: EntityRendererProvider.Context) :
    EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimated {

    private var hasHeadLayer = false

    override fun getTextureLocation(entity: T): ResourceLocation {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        yaw: Float,
        partialTick: Float,
        stack: PoseStack,
        p_225623_5_: MultiBufferSource,
        packedLight: Int,
    ) {
        val model = MCglTF.getOrCreate(entity.model)
        val manager = entity.manager as ClientModelManager

        val type = getRenderType(entity)

        preRender(entity, manager, stack, partialTick)

        val currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)
        val currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)
        val currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        stack.pushPose()

        val lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        stack.mulPose(Vector3f.YP.rotationDegrees(-lerpBodyRot))

        RenderedGltfModel.setCurrentPose(stack.last().pose())
        RenderedGltfModel.setCurrentNormal(stack.last().normal())
        stack.popPose()

        GL30.glVertexAttribI2i(
            RenderedGltfModel.vaUV2,
            packedLight and '\uffff'.code,
            packedLight shr 16 and '\uffff'.code
        )

        GL30.glVertexAttribI2i(
            RenderedGltfModel.vaUV1,
            (partialTick * 15).toInt(),
            if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10
        )

        type.setupRenderState()
        if (MCglTF.getInstance().isShaderModActive) {
            model.renderedGltfScenes.forEach { it.renderForShaderMod() }
        } else {

            GL13.glActiveTexture(GL13.GL_TEXTURE2) //Лайтмап
            val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)
            GL13.glActiveTexture(GL13.GL_TEXTURE1) //Оверлей
            val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            val overlay = if (entity.hurtTime > 0 || !entity.isAlive) RenderSystem.getShaderTexture(1) else 0
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlay)
            GL13.glActiveTexture(GL13.GL_TEXTURE0) //Текстуры модели
            val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

            model.renderedGltfScenes.forEach { it.renderForVanilla() }

            if (Minecraft.getInstance().shouldEntityAppearGlowing(entity)) {
                val outline = type.outline().getOrNull() ?: return
                outline.setupRenderState()

                GL13.glActiveTexture(GL13.GL_TEXTURE2) //Лайтмап
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().lightTexture.id)

                GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 255, 255) //нужно для чистого белого цвета

                GL13.glActiveTexture(GL13.GL_TEXTURE1) //Оверлей
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1))

                GL13.glActiveTexture(GL13.GL_TEXTURE0) //Текстуры модели
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().defaultColorMap)

                GL13.glActiveTexture(GL13.GL_TEXTURE10) //Текстуры модели

                model.renderedGltfScenes.forEach { it.renderForVanilla() }

                outline.clearRenderState()
            }

            GL13.glActiveTexture(GL13.GL_TEXTURE2) //Возврат Лайтмапа
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
            GL13.glActiveTexture(GL13.GL_TEXTURE1) //Возврат Оверлея
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
            GL13.glActiveTexture(GL13.GL_TEXTURE0) //Возврат Исходных текстур
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
        }
        type.clearRenderState()

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0)
        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0)

        GL30.glBindVertexArray(currentVAO)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        super.render(entity, yaw, partialTick, stack, p_225623_5_, packedLight)
    }

    protected fun getRenderType(
        entity: T,
    ): RenderType {
        val location = getTextureLocation(entity)
        return if (!entity.isInvisible && entity.isInvisibleTo(Minecraft.getInstance().player!!)) {
            RenderType.itemEntityTranslucentCull(location)
        } else RenderType.entityTranslucent(location)
    }


    private fun preRender(entity: T, manager: ClientModelManager, stack: PoseStack, partialTick: Float) {
        stack.mulPoseMatrix(manager.transform.matrix)

        if (!hasHeadLayer) {
            hasHeadLayer = true
            manager.addLayer(HeadLayer(entity, 1.0f))
        }

        updateAnimations(entity, manager)
        manager.update(partialTick)
    }

    private fun updateAnimations(entity: T, manager: GLTFAnimationManager) {
        val templates = manager.templates

        if (!entity.isAlive) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.DEATH, "")
            return
        }

        if (entity is FlyingAnimal) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.FLY, "")
            return
        }

        if (entity.isSleeping) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.SLEEP, "")
            return
        }

        if (entity.swinging) {
//            val anim = AnimationLoader.createAnimation(
//                renderedScene.gl!!.model?.gltfModel ?: return,
//                templates.getOrDefault(AnimationType.SWING, "")
//            ) ?: return
            //manager.addLayer(anim)
            return
        }

        entity.vehicle?.let {
            manager.currentAnimation = templates.getOrDefault(AnimationType.SIT, "")
            return
        }

        if (entity.fallFlyingTicks > 4) {
            manager.currentAnimation = templates.getOrDefault(AnimationType.FALL, "")
            return
        }

        manager.currentAnimation = if (entity.animationSpeed > 0.01) {
            templates.getOrDefault(
                if (entity.isVisuallySwimming) AnimationType.SWIM
                else if (entity.animationSpeed > 1.5f) AnimationType.RUN
                else if (entity.isShiftKeyDown) AnimationType.WALK_SNEAKED
                else AnimationType.WALK, ""
            )
        } else {
            templates.getOrDefault(
                if (entity.isShiftKeyDown) AnimationType.IDLE_SNEAKED else AnimationType.IDLE,
                ""
            )
        }
    }
}