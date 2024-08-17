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
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.ModelData
import ru.hollowhorizon.hc.client.models.gltf.animations.SubModelPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.*
import ru.hollowhorizon.hc.client.utils.SkinDownloader
import ru.hollowhorizon.hc.client.utils.memoize
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.use


object GltfEntityUtil {
    lateinit var itemRenderer: ItemInHandRenderer

    @Suppress("DEPRECATION")
    fun render(
        entity: LivingEntity,
        model: SubModel,
        tickCount: Int,
        partialTick: Float,
        stack: PoseStack,
        source: MultiBufferSource,
        packedLight: Int,
    ) {
        if (model.model == "%NO_MODEL%") return

        val realModel = GltfManager.getOrCreate(model.model.rl)

        realModel.visuals = ::drawVisuals

        //? if <1.21 {
        stack.mulPoseMatrix(model.transform.matrix)
        //?} else {
        /*
        stack.mulPose(model.transform.matrix)
        *///?}

        SubModelPlayer.update(realModel, model, tickCount, partialTick)

        realModel.render(
            stack,
            ModelData(null, null, itemRenderer, null),
            { texture: ResourceLocation ->
                val result = model.textures[texture.path]?.let {
                    if (it.startsWith("skins/")) SkinDownloader.downloadSkin(it.substring(6))
                    else it.rl
                } ?: texture

                Minecraft.getInstance().textureManager.getTexture(result).id
            }.memoize(),
            source,
            packedLight, OverlayTexture.pack(0, if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10)
        )

        model.subModels.forEach { (bone, model) ->
            realModel.nodes[bone]?.let {
                stack.use {
                    //? if <1.21 {
                    stack.mulPoseMatrix(it.globalMatrix)
                    //?} else {
                    /*
                    stack.mulPose(it.globalMatrix)
                    *///?}
                    render(entity, model, tickCount, partialTick, stack, source, packedLight)
                }
            }
        }
    }

    private fun drawVisuals(entity: LivingEntity, stack: PoseStack, node: GltfTree.Node, source: MultiBufferSource, light: Int) {
        if ((node.name?.contains("left", ignoreCase = true) == true || node.name?.contains(
                "right",
                ignoreCase = true
            ) == true) &&
            node.name.contains("hand", ignoreCase = true) &&
            node.name.contains("item", ignoreCase = true)
        ) {
            val isLeft = node.name.contains("left", ignoreCase = true)
            val item = (if (isLeft) entity.getItemInHand(InteractionHand.OFF_HAND) else entity.getItemInHand(
                InteractionHand.MAIN_HAND
            )) ?: return

            stack.pushPose()
            stack.mulPose(Quaternionf().rotateX(-90.0f * Mth.DEG_TO_RAD))

            itemRenderer.renderItem(
                entity,
                item,
                if (isLeft) ItemDisplayContext.THIRD_PERSON_LEFT_HAND else ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                isLeft,
                stack,
                Minecraft.getInstance().renderBuffers().bufferSource(),
                light
            )

            stack.popPose()
        }
    }
}