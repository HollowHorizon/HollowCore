package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Vector3f
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.ModelData
import ru.hollowhorizon.hc.client.models.gltf.animations.SubModelPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.*
import ru.hollowhorizon.hc.client.utils.SkinDownloader
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.use

object GltfEntityUtil {
    lateinit var itemRenderer: ItemInHandRenderer
    private val renderType = Util.memoize { texture: ResourceLocation, data: Boolean ->
        val state =
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(data)
        RenderType.create(
            "hc:gltf_entity", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES,
            256, true, true, state
        )
    }

    @Suppress("DEPRECATION")
    fun render(
        entity: LivingEntity,
        model: SubModel,
        tickCount: Int,
        partialTick: Float,
        stack: PoseStack,
        packedLight: Int,
    ) {
        if(model.model == "%NO_MODEL%") return

        val realModel = GltfManager.getOrCreate(model.model.rl)

        realModel.visuals = ::drawVisuals

        stack.mulPoseMatrix(model.transform.matrix)

        SubModelPlayer.update(realModel, model, tickCount, partialTick)

        realModel.render(
            stack,
            ModelData(null, null, itemRenderer, null),
            { texture: ResourceLocation ->
                val result = model.textures[texture.path]?.let {
                    if (it.startsWith("skins/")) SkinDownloader.downloadSkin(it.substring(6))
                    else it.rl
                } ?: texture

                renderType.apply(result, true)
            },
            packedLight, OverlayTexture.pack(0, if (entity.hurtTime > 0 || !entity.isAlive) 3 else 10)
        )

        model.subModels.forEach { (bone, model) ->
            realModel.nodes[bone]?.let {
                stack.use {
                    stack.mulPoseMatrix(it.globalMatrix)
                    render(entity, model, tickCount, partialTick, stack, packedLight)
                }
            }
        }
    }

    private fun drawVisuals(entity: LivingEntity, stack: PoseStack, node: GltfTree.Node, light: Int) {
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
            stack.mulPose(Vector3f.XP.rotationDegrees(-90.0f))

            itemRenderer.renderItem(
                entity,
                item,
                if (isLeft) ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND else ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                isLeft,
                stack,
                Minecraft.getInstance().renderBuffers().bufferSource(),
                light
            )

            stack.popPose()
        }
    }
}