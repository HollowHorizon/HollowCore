package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.LivingRenderer
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.IFlyingAnimal
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.animation.AnimationTypes
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2


class GLTFEntityRenderer<T>(manager: EntityRendererManager) :
        EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimatedEntity {

    override fun getTextureLocation(entity: T): ResourceLocation {
        return ResourceLocation("hc", "textures/entity/test_entity.png")
    }

    private fun preRender(entity: T, stack: MatrixStack, partialTick: Float) {
        val capability = entity.getCapability(HollowCapabilityV2.get<AnimatedEntityCapability>())
                .orElseThrow { IllegalStateException("Animated Entity Capability Not Found!") }

        stack.last().pose().multiply(capability.transform)

        val templates = capability.animations
        val manager = capability.manager

        updateAnimations(entity, manager, templates)

        manager.markedToRemove.forEach {
            manager.animations.remove(it)
        }

        manager.animations.removeIf { animation ->
            entity.animationList.find { anim -> anim.name == animation.name }
                    ?.update(animation.tick(partialTick), animation.loop) ?: false
        }
    }

    private fun updateAnimations(entity: T, manager: GLTFAnimationManager, templates: HashMap<AnimationTypes, String>) {
        if (!entity.isAlive) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.DEATH, ""), true)
            return
        }

        if(entity is IFlyingAnimal) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.FLY, ""), true)
            return
        }

        if(entity.isSleeping) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.SLEEP, ""), true)
            return
        }

        if(entity.swinging) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.SWING, ""))
        }

        if (entity.vehicle != null) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.SIT, ""), true)
            return
        }

        if (entity.fallFlyingTicks > 4) {
            manager.setAnimation(templates.getOrDefault(AnimationTypes.FALL, ""), true)
            return
        }

        if (entity.animationSpeed > 0.01) {
            val animation =
                    if (entity.isVisuallySwimming) AnimationTypes.SWIM
                    else if (entity.animationSpeed > 1.5f) AnimationTypes.RUN
                    else if (entity.isShiftKeyDown) AnimationTypes.WALK_SNEAKED
                    else AnimationTypes.WALK

            manager.setAnimation(templates.getOrDefault(animation, ""))
        } else {
            manager.setAnimation(templates.getOrDefault(if (entity.isShiftKeyDown) AnimationTypes.IDLE_SNEAKED else AnimationTypes.IDLE, ""), true)
        }
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        yaw: Float,
        particalTick: Float,
        stack: MatrixStack,
        p_225623_5_: IRenderTypeBuffer,
        packedLight: Int
    ) {
        super.render(entity, yaw, particalTick, stack, p_225623_5_, packedLight)
        if(entity.renderedGltfModel == null) return

        stack.pushPose()

        preRender(entity, stack, particalTick)

        val packedOverlay: Int = LivingRenderer.getOverlayCoords(entity, particalTick)

        GL11.glPushMatrix()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_COLOR_MATERIAL)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        )

        GL11.glAlphaFunc(516, 0.1f)

        RenderSystem.multMatrix(stack.last().pose())
        GL11.glRotatef(-yaw, 0.0f, 1.0f, 0.0f)

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
            entity.renderedGltfModel?.renderedGltfScenes?.forEach { it.renderForShaderMod() }
        } else {
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            entity.renderedGltfModel?.renderedGltfScenes?.forEach { it.renderForVanilla() }
        }

        GL11.glPopAttrib()
        GL11.glPopMatrix()

        stack.popPose()
    }
}