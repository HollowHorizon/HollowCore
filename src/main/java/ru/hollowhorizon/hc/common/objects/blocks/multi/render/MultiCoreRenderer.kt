package ru.hollowhorizon.hc.common.objects.blocks.multi.render

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.block.HorizontalFaceBlock
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.util.Direction
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiCoreBlockEntity

class MultiCoreRenderer(p_i226006_1_: TileEntityRendererDispatcher) : TileEntityRenderer<MultiCoreBlockEntity>(
    p_i226006_1_
) {
    override fun render(
        tile: MultiCoreBlockEntity,
        partialTicks: Float,
        stack: MatrixStack,
        bufferIn: IRenderTypeBuffer,
        packedLightIn: Int,
        p_225616_6_: Int
    ) {
        val facing: Direction = tile.blockState.getValue(HorizontalFaceBlock.FACING)

        when(facing) {
            Direction.NORTH -> stack.translate(tile.offset.x, tile.offset.y, tile.offset.z)
            Direction.SOUTH -> stack.translate(-tile.offset.x, tile.offset.y, -tile.offset.z)
            Direction.EAST -> stack.translate(-tile.offset.z, tile.offset.y, tile.offset.x)
            Direction.WEST -> stack.translate(tile.offset.z, tile.offset.y, -tile.offset.x)
            else -> {}
        }
    }

    override fun shouldRenderOffScreen(p_188185_1_: MultiCoreBlockEntity): Boolean {
        return true
    }


}