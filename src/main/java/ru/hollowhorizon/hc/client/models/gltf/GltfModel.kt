package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.models.gltf.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability


class ModelData(
    val leftHand: ItemStack?,
    val rightHand: ItemStack?,
    val itemInHandRenderer: ItemInHandRenderer?,
    val entity: LivingEntity?,
)

typealias NodeRenderer = (LivingEntity, PoseStack, GltfTree.Node, Int) -> Unit

class GltfModel(val modelTree: GltfTree.GLTFTree) {
    val bindPose = Animation.createFromPose(modelTree.walkNodes())
    val animationPlayer = GLTFAnimationPlayer(this)
    var visuals: NodeRenderer = { _, _, _, _ -> }

    fun update(capability: AnimatedEntityCapability, currentTick: Int, partialTick: Float) {
        animationPlayer.setTick(currentTick)
        animationPlayer.update(capability, partialTick)
    }

    fun entityUpdate(entity: LivingEntity, capability: AnimatedEntityCapability, partialTick: Float) {
        animationPlayer.updateEntity(entity, capability, partialTick)
    }

    fun render(
        stack: PoseStack,
        modelData: ModelData,
        consumer: (ResourceLocation) -> VertexConsumer,
        light: Int,
        overlay: Int,
    ) {
        modelTree.scenes.forEach { it.render(stack, visuals, modelData, consumer, light, overlay) }
        NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear()
    }
}

private fun Vector4f.isNotEmpty(): Boolean {
    return x() != 0f || y() != 0f || z() != 0f || w() != 0f
}
