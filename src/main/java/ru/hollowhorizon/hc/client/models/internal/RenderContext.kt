package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

class RenderContext(
    val stack: PoseStack,
    val consumer: (ResourceLocation) -> Int,
    val buffer: MultiBufferSource,
    val packedLight: Int,
    val packedOverlay: Int,
    val nodeRenderer: NodeRenderer = { _, _, _, _, _ -> },
    val entity: LivingEntity? = null,
)

class RenderCommands {
    val drawCommands: MutableList<RenderContext.() -> Unit> = ArrayList()
    val skinningCommands: MutableList<() -> Unit> = ArrayList()
}