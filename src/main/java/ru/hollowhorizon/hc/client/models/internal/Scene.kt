package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation

data class Scene(
    val nodes: List<Node>,
) {
    fun render(
        stack: PoseStack,
        nodeRenderer: NodeRenderer,
        data: ModelData,
        consumer: (ResourceLocation) -> Int,
        light: Int,
    ) {
        nodes.forEach { it.render(stack, nodeRenderer, data, consumer, light) }
    }

    fun transformSkinning(stack: PoseStack) {
        nodes.forEach { it.transformSkinning(stack) }
    }
}