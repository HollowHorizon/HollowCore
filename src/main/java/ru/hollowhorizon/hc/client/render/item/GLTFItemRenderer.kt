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

package ru.hollowhorizon.hc.client.render.item



import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.models.internal.ModelData
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationType
import ru.hollowhorizon.hc.client.models.internal.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.client.utils.SkinDownloader
import ru.hollowhorizon.hc.client.utils.get
//? if <=1.19.2 {
import ru.hollowhorizon.hc.client.utils.math.mul
import ru.hollowhorizon.hc.client.utils.math.mulPose
import ru.hollowhorizon.hc.client.utils.math.mulPoseMatrix
//?} else {
/*import net.minecraft.world.item.ItemDisplayContext
*///?}
import ru.hollowhorizon.hc.client.utils.memoize
import ru.hollowhorizon.hc.client.utils.rl




class GLTFItemRenderer() : BlockEntityWithoutLevelRenderer(
    Minecraft.getInstance().blockEntityRenderDispatcher,
    Minecraft.getInstance().entityModels
) {

    override fun renderByItem(
        item: ItemStack,
        //? if <=1.19.2 {
        transformType: ItemTransforms.TransformType,
        //?} else {
        /*transforms: ItemDisplayContext,
        *///?}
        stack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        val capability = item[AnimatedEntityCapability::class]
        val modelPath = capability.model
        if (modelPath == GLTFEntityRenderer.NO_MODEL) return

        val model = GltfManager.getOrCreate(modelPath.rl)

        stack.pushPose()

        preRender(capability, model.animationPlayer, stack)
        model.update(capability, TickHandler.currentTicks, TickHandler.partialTick)

        model.render(
            stack,
            ModelData(null, null, Minecraft.getInstance().gameRenderer.itemInHandRenderer, null),
            { texture: ResourceLocation ->
                val result = capability.textures[texture.path]?.let {
                    if (it.startsWith("skins/")) SkinDownloader.downloadSkin(it.substring(6))
                    else it.rl
                } ?: texture

                Minecraft.getInstance().textureManager.getTexture(result).id
            }.memoize(),
            buffer,
            packedLight,
            packedOverlay
        )

        stack.popPose()
    }

    private fun preRender(
        capability: AnimatedEntityCapability,
        animationPlayer: GLTFAnimationPlayer,
        stack: PoseStack,
    ) {
        //? if <1.21 {
        stack.mulPoseMatrix(capability.transform.matrix)
        //?} else {

        /*stack.mulPose(capability.transform.matrix)
        *///?}
        stack.last().normal().mul(capability.transform.normalMatrix)
        stack.mulPose(Quaternionf().rotateY(180f * Mth.DEG_TO_RAD))
        animationPlayer.currentLoopAnimation = AnimationType.IDLE
    }
}