package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation

data class Mesh(
    val primitives: List<Primitive>,
    val weights: List<Float>,
) {
    fun render(
        node: Node,
        stack: PoseStack,
        consumer: (ResourceLocation) -> Int,
    ) {
        primitives.forEach {
            it.render(stack, node, consumer)
        }
    }

    fun transformSkinning(node: Node, stack: PoseStack) {
        primitives.filter { it.hasSkinning }.forEach { it.transformSkinning(node, stack) }
    }
}