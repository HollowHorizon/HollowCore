package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Vector3f
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderType.CompositeState
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import ru.hollowhorizon.hc.client.gltf.animations.GLTFAnimationManager
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.ClientModelManager
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage


class GLTFEntityRenderer<T>(manager: EntityRendererProvider.Context) :
    EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimated {

    val renderType = Util.memoize { texture: ResourceLocation, data: Boolean ->
        val compositestate: CompositeState =
            CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP).setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(data)
        RenderType.create(
            "hc:gltf_entity", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES,
            256, true, true, compositestate
        )
    }

    override fun getTextureLocation(entity: T): ResourceLocation {
        //return "hc:textures/entity/vuzz.png".rl
        return TextureManager.INTENTIONAL_MISSING_TEXTURE
    }

    @Suppress("DEPRECATION")
    override fun render(
        entity: T,
        yaw: Float,
        partialTick: Float,
        stack: PoseStack,
        source: MultiBufferSource,
        packedLight: Int,
    ) {
        super.render(entity, yaw, partialTick, stack, source, packedLight)

        val model = GltfManager.getOrCreate(entity.model)
        val manager = entity.manager as ClientModelManager
        manager.setTick(entity.tickCount)

        val type = getRenderType(entity)

        preRender(entity, manager, stack, partialTick)

        stack.pushPose()

        val lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        stack.mulPose(Vector3f.YP.rotationDegrees(-lerpBodyRot))

        model.render(
            stack,
            source.getBuffer(type),
            packedLight,
            OverlayTexture.pack(0, if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10)
        )
        stack.popPose()
    }

    protected fun getRenderType(
        entity: T,
    ): RenderType {
        val location = getTextureLocation(entity)
        return renderType.apply(location, true)
    }


    private fun preRender(entity: T, manager: ClientModelManager, stack: PoseStack, partialTick: Float) {
        val capability = entity.getCapability(CapabilityStorage.getCapability(AnimatedEntityCapability::class.java))
            .orElseThrow { IllegalStateException("AnimatedEntityCapability is missing") }
        stack.mulPoseMatrix(capability.transform.matrix)
        stack.mulPose(Vector3f.YP.rotationDegrees(180f))

        manager.updateEntity(entity)

        updateAnimations(entity, manager, capability.customAnimations)
        manager.update(partialTick)
    }

    private fun updateAnimations(entity: T, manager: GLTFAnimationManager, overrides: Map<AnimationType, String>) {
        val templates = manager.templates + overrides

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