package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.animations.AnimationLoader
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import ru.hollowhorizon.hc.common.capabilities.getCapability


class GLTFEntityRenderer<T>(manager: EntityRendererProvider.Context) :
    EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimatedEntity {


    override fun getTextureLocation(entity: T): ResourceLocation {
        return ResourceLocation("hc", "textures/entity/test_entity.png")
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        yaw: Float,
        particalTick: Float,
        stack: PoseStack,
        p_225623_5_: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, yaw, particalTick, stack, p_225623_5_, packedLight)
        if (entity.model == null) return

        stack.pushPose()

        preRender(entity, stack, particalTick)

        val packedOverlay: Int = LivingEntityRenderer.getOverlayCoords(entity, particalTick)

        val currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)
        val currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)
        val currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        val currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE)

        val currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST)
        val currentBlend = GL11.glGetBoolean(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        stack.pushPose()
        stack.mulPose(Vector3f.YP.rotationDegrees(Mth.rotLerp(particalTick, entity.yBodyRotO, entity.yBodyRot)))
        RenderedGltfModel.setCurrentPose(stack.last().pose())
        RenderedGltfModel.setCurrentNormal(stack.last().normal())
        stack.popPose()

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, packedLight and '\uffff'.code, packedLight shr 16 and '\uffff'.code)

        if (GlTFModelManager.getInstance().isShaderModActive()) {
            entity.model?.renderedGltfScenes?.forEach { it.renderForShaderMod() }
        } else {
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlTFModelManager.getInstance().lightTexture.id)
            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            entity.model?.renderedGltfScenes?.forEach { it.renderForVanilla() }
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
        }

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0)

        if (!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST)
        if (!currentBlend) GL11.glDisable(GL11.GL_BLEND)

        if (currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE) else GL11.glDisable(GL11.GL_CULL_FACE)

        GL30.glBindVertexArray(currentVAO)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        stack.popPose()
    }

    private fun preRender(entity: T, stack: PoseStack, partialTick: Float) {
        val capability = entity.getCapability(AnimatedEntityCapability::class)

        //Изменение начального положения модели
        stack.last().pose().multiply(Matrix4f().apply {
            setIdentity()
            translate(capability.transform.vecTransform())
            multiply(Vector3f.XP.rotationDegrees(capability.transform.rX))
            multiply(Vector3f.YP.rotationDegrees(capability.transform.rY))
            multiply(Vector3f.ZP.rotationDegrees(capability.transform.rZ))
            multiply(
                Matrix4f.createScaleMatrix(
                    capability.transform.sX,
                    capability.transform.sY,
                    capability.transform.sZ
                )
            )
        })

        val templates = capability.animations
        val manager = entity.model?.manager ?: return

        updateAnimations(entity, manager, templates)

        if (capability.animationsToStart.isNotEmpty()) {
            capability.animationsToStart.forEach(manager::addAnimation)
            capability.animationsToStart.clear()
        }
        if (capability.animationsToStop.isNotEmpty()) {
            capability.animationsToStop.forEach(manager::removeAnimation)
            capability.animationsToStart.clear()
        }

        manager.update(entity.tickCount, partialTick)
    }

    private fun updateAnimations(entity: T, manager: AnimationManager, templates: HashMap<AnimationType, String>) {
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
            val anim = AnimationLoader.createAnimation(
                entity.model?.gltfModel ?: return,
                templates.getOrDefault(AnimationType.SWING, "")
            ) ?: return
            manager.addLayer(anim)
            return
        }

        if (entity.vehicle != null) {
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