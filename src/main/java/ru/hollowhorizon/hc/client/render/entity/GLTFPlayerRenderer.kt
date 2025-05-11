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
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.models.internal.ModelData
import ru.hollowhorizon.hc.client.models.internal.Node
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationType
import ru.hollowhorizon.hc.client.models.internal.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.internal.animations.PlayMode
import ru.hollowhorizon.hc.client.models.internal.manager.*
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer.Companion.MOVEMENT_FACTOR
import ru.hollowhorizon.hc.client.utils.*
import ru.hollowhorizon.hc.common.utils.get
import ru.hollowhorizon.hc.common.utils.memoize
import ru.hollowhorizon.hc.common.utils.rl
import ru.hollowhorizon.hc.fabric.internal.IrisHelper
import kotlin.math.abs

object GLTFPlayerRenderer {
    private val itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer

    fun render(
        entity: Player,
        partialTick: Float,
        stack: PoseStack,
        source: MultiBufferSource,
        packedLight: Int,
    ) {
        val capability = entity[AnimatedEntityCapability::class]
        val modelPath = capability.model
        if (modelPath == GLTFEntityRenderer.NO_MODEL) return

        val model = GltfManager.getOrCreate(modelPath.rl)

        stack.pushPose()

        preRender(entity, capability, model.animationPlayer, stack)

        val lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        stack.mulPose(Quaternionf().rotateY(-lerpBodyRot * Mth.DEG_TO_RAD))

        model.visuals = ::drawVisuals

        // Без этого от 1 лица не будет обновляться тень
        IrisHelper.bypassShadow = Minecraft.getInstance().options.cameraType.isFirstPerson
        model.entityUpdate(entity, capability, partialTick)
        model.update(capability)
        IrisHelper.bypassShadow = false

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
                    stack.mulPoseMatrix(it.globalMatrix)
                    GltfEntityUtil.render(entity, child, entity.tickCount, partialTick, stack, source, packedLight)
                }
            }
        }

        stack.popPose()
    }

    private fun drawVisuals(
        entity: LivingEntity,
        stack: PoseStack,
        node: Node,
        source: MultiBufferSource,
        light: Int,
    ) {
        if ((node.name?.contains("left", ignoreCase = true) == true || node.name?.contains(
                "right",
                ignoreCase = true
            ) == true) &&
            node.name.contains("hand", ignoreCase = true) &&
            node.name.contains("item", ignoreCase = true)
        ) {
            val isLeft = node.name.contains("left", ignoreCase = true)
            val item =
                (if (isLeft) entity.getItemInHand(InteractionHand.OFF_HAND) else entity.getItemInHand(InteractionHand.MAIN_HAND)) ?: return

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
        entity: Player,
        capability: AnimatedEntityCapability,
        manager: GLTFAnimationPlayer,
        stack: PoseStack,
    ) {
        stack.mulPoseMatrix(capability.transform.matrix)
        stack.last().normal().mul(capability.transform.normalMatrix)
        stack.mulPose(Quaternionf().rotateY(180f * Mth.DEG_TO_RAD))
        GLTFEntityRenderer.updateAnimations(entity, capability, manager)
    }
}
