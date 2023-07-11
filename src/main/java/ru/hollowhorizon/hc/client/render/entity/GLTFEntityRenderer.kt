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
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.animations.AnimationLoader
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import kotlin.properties.Delegates


class GLTFEntityRenderer<T>(manager: EntityRendererManager) :
    EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimatedEntity {
    private var currentAnimation: String by Delegates.observable("") { property, oldValue, newValue ->
        changed = oldValue != newValue
    }
    var changed = false

    override fun getTextureLocation(entity: T): ResourceLocation {
        return ResourceLocation("hc", "textures/entity/test_entity.png")
    }

    private fun preRender(entity: T, stack: MatrixStack, partialTick: Float) {
        val capability = entity.getCapability(HollowCapabilityV2.get<AnimatedEntityCapability>())
            .orElseThrow { IllegalStateException("Animated Entity Capability Not Found!") }

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
        val manager = entity.animationManager ?: return

        updateAnimations(entity, manager, templates)

        if (changed && currentAnimation != "") {
            manager.setAnimation(currentAnimation)
            changed = false
        }

        manager.update(partialTick)
    }

    private fun updateAnimations(entity: T, manager: AnimationManager, templates: HashMap<AnimationType, String>) {
        if (!entity.isAlive) {
            currentAnimation = templates.getOrDefault(AnimationType.DEATH, "")
            return
        }

        if (entity is IFlyingAnimal) {
            currentAnimation = templates.getOrDefault(AnimationType.FLY, "")
            return
        }

        if (entity.isSleeping) {
            currentAnimation = templates.getOrDefault(AnimationType.SLEEP, "")
            return
        }

        if (entity.swinging) {
            val anim = AnimationLoader.createAnimation(
                entity.renderedGltfModel?.gltfModel ?: return,
                templates.getOrDefault(AnimationType.SWING, "")
            ) ?: return
            manager.addLayer(anim)
            return
        }

        if (entity.vehicle != null) {
            currentAnimation = templates.getOrDefault(AnimationType.SIT, "")
            return
        }

        if (entity.fallFlyingTicks > 4) {
            currentAnimation = templates.getOrDefault(AnimationType.FALL, "")
            return
        }

        currentAnimation = if (entity.animationSpeed > 0.01) {
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
        if (entity.renderedGltfModel == null) return

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