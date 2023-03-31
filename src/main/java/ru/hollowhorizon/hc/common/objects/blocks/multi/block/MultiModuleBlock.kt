package ru.hollowhorizon.hc.common.objects.blocks.multi.block

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiCoreBlockEntity
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiModuleBlockEntity

class MultiModuleBlock(properties: Properties?) : Block(properties!!) {
    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.INVISIBLE", "net.minecraft.block.BlockRenderType"))
    override fun getRenderShape(p_149645_1_: BlockState): BlockRenderType {
        return BlockRenderType.INVISIBLE
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "super.use(state, level, pos, player, hand, hit)",
            "net.minecraft.block.Block"
        )
    )
    override fun use(
        state: BlockState,
        level: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockRayTraceResult
    ): ActionResultType {
        val tile = level.getBlockEntity(pos) as MultiModuleBlockEntity

        if (tile.corePos == null) return super.use(state, level, pos, player, hand, hit)

        val coreTile = level.getBlockEntity(tile.corePos) as MultiCoreBlockEntity

        coreTile.onOpen(player, pos)

        return super.use(state, level, pos, player, hand, hit)
    }

    override fun onRemove(state: BlockState, level: World, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        level.getBlockEntity(pos)?.let { (it as MultiModuleBlockEntity).breakMultiBlock() }
        super.onRemove(state, level, pos, newState, isMoving)
    }

    override fun hasTileEntity(state: BlockState?): Boolean {
        return true
    }

    override fun propagatesSkylightDown(state: BlockState?, level: IBlockReader?, pos: BlockPos?): Boolean {
        return true
    }

    @OnlyIn(Dist.CLIENT)
    override fun getShadeBrightness(state: BlockState?, level: IBlockReader?, pos: BlockPos?): Float {
        return 1.0f
    }

    override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
        return MultiModuleBlockEntity()
    }
}