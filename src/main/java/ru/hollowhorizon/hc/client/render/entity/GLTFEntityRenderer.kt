/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import net.minecraft.world.item.ItemDisplayContext
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.ModelData
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.models.gltf.manager.*
import ru.hollowhorizon.hc.client.utils.*


open class GLTFEntityRenderer<T>(manager: EntityRendererProvider.Context) :
    EntityRenderer<T>(manager) where T : LivingEntity, T : IAnimated {
    val itemInHandRenderer = manager.itemInHandRenderer.apply {
        GltfEntityUtil.itemRenderer = this
    }

    override fun getTextureLocation(entity: T): ResourceLocation {
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

        val capability = entity[AnimatedEntityCapability::class]
        val modelPath = capability.model
        if (modelPath == NO_MODEL) return

        val model = GltfManager.getOrCreate(modelPath.rl)

        stack.pushPose()

        preRender(entity, capability, model.animationPlayer, stack)

        val lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        stack.mulPose(Quaternionf().rotateY(-lerpBodyRot * Mth.DEG_TO_RAD))

        model.visuals = ::drawVisuals

        model.update(capability, entity.tickCount, partialTick)
        model.entityUpdate(entity, capability, partialTick)

        model.render(
            stack,
            ModelData(entity.offhandItem, entity.mainHandItem, itemInHandRenderer, entity),
            { texture: ResourceLocation ->
                val result = capability.textures[texture.path]?.let {
                    if (it.startsWith("skins/")) SkinDownloader.downloadSkin(it.substring(6))
                    else it.rl
                } ?: texture

                Minecraft.getInstance().textureManager.getTexture(result).id
            }.memoize(),
            source,
            packedLight,
            OverlayTexture.pack(0, if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10)
        )

        capability.subModels.forEach { (node, child) ->
            model.nodes[node]?.let {
                stack.use {
                    //? if <1.21 {
                    /*stack.mulPoseMatrix(it.globalMatrix)
                    *///?} else {
                    
                    stack.mulPose(it.globalMatrix)
                    //?}
                    GltfEntityUtil.render(entity, child, entity.tickCount, partialTick, stack, source, packedLight)
                }
            }
        }

        stack.popPose()
    }

    protected open fun drawVisuals(entity: LivingEntity, stack: PoseStack, node: GltfTree.Node, source: MultiBufferSource, light: Int) {
        if ((node.name?.contains("left", ignoreCase = true) == true || node.name?.contains(
                "right",
                ignoreCase = true
            ) == true) &&
            node.name.contains("hand", ignoreCase = true) &&
            node.name.contains("item", ignoreCase = true)
        ) {
            val isLeft = node.name.contains("left", ignoreCase = true)
            val item =
                (if (isLeft) entity.getItemInHand(InteractionHand.OFF_HAND) else entity.getItemInHand(InteractionHand.MAIN_HAND))
                    ?: return

            stack.pushPose()
            stack.mulPose(Quaternionf().rotateX(-90 * Mth.DEG_TO_RAD))

            itemInHandRenderer.renderItem(
                entity,
                item,
                if (isLeft) ItemDisplayContext.THIRD_PERSON_LEFT_HAND else ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                isLeft,
                stack,
                source,
                light
            )

            stack.popPose()
        }
    }

    private fun preRender(
        entity: T,
        capability: AnimatedEntityCapability,
        manager: GLTFAnimationPlayer,
        stack: PoseStack,
    ) {
        //? if <1.21 {
        /*stack.mulPoseMatrix(capability.transform.matrix)
        *///?} else {
        
        stack.mulPose(capability.transform.matrix)
        //?}
        stack.last().normal().mul(capability.transform.normalMatrix)
        stack.mulPose(Quaternionf().rotateY(180f * Mth.DEG_TO_RAD))
        updateAnimations(entity, capability, manager)
    }

    private fun updateAnimations(entity: T, capability: AnimatedEntityCapability, manager: GLTFAnimationPlayer) {
        val layers = capability.layers
        when {
            entity.hurtTime > 0 -> {
                val name = manager.typeToAnimationMap[AnimationType.HURT]?.name ?: return
                if (layers.any { it.animation == name }) {
                    layers.filter { it.animation == name }.forEach { it.time = 0 }
                    return
                }

                layers += AnimationLayer(
                    name,
                    LayerMode.ADD,
                    PlayMode.ONCE,
                    1.0f, fadeIn = 5
                )
            }

            entity.swinging -> {
                val name = manager.typeToAnimationMap[AnimationType.SWING]?.name ?: return
                if (layers.any { it.animation == name }) return

                layers += AnimationLayer(
                    name,
                    LayerMode.ADD,
                    PlayMode.ONCE,
                    1.0f, fadeIn = 5
                )
            }

            !entity.isAlive -> {
                val name = manager.typeToAnimationMap[AnimationType.DEATH]?.name ?: return
                if (layers.any { it.animation == name }) return

                layers += AnimationLayer(
                    name,
                    LayerMode.ADD,
                    PlayMode.LAST_FRAME,
                    1.0f, fadeIn = 5
                )
            }
        }
        manager.currentLoopAnimation = when {
            entity is FlyingAnimal && entity.isFlying -> AnimationType.FLY
            entity.isSleeping -> AnimationType.SLEEP
            entity.vehicle != null -> AnimationType.SIT
            entity.fallFlyingTicks > 4 -> AnimationType.FALL
            entity.deltaMovement.length() > 0.075 -> {
                when {
                    entity.isVisuallySwimming -> AnimationType.SWIM
                    entity.isShiftKeyDown -> AnimationType.WALK_SNEAKED
                    entity.isSprinting -> AnimationType.RUN
                    else -> AnimationType.WALK
                }
            }

            else -> AnimationType.IDLE
        }
    }

    companion object {
        const val NO_MODEL = "%NO_MODEL%"
    }
}
