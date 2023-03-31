package ru.hollowhorizon.hc.common.objects.blocks.multi.block

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
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
import net.minecraftforge.common.ToolType
import ru.hollowhorizon.hc.common.objects.blocks.multi.entity.MultiCoreBlockEntity

class MultiCoreBlock : Block(Properties.of(Material.METAL).harvestLevel(1).harvestTool(ToolType.PICKAXE).strength(0.3f).noOcclusion()) {
    override fun onRemove(state: BlockState, level: World, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        level.getBlockEntity(pos)?.let { (it as MultiCoreBlockEntity).breakMultiBlock() }
        super.onRemove(state, level, pos, newState, isMoving)
    }

    override fun use(
        state: BlockState, level: World, pos: BlockPos,
        player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult
    ): ActionResultType {
        val tile = level.getBlockEntity(pos) as MultiCoreBlockEntity
        tile.onOpen(player, pos)

        return super.use(state, level, pos, player, hand, hit)
    }

    override fun getRenderShape(p_149645_1_: BlockState): BlockRenderType {
        return BlockRenderType.INVISIBLE
    }

    override fun propagatesSkylightDown(state: BlockState?, level: IBlockReader?, pos: BlockPos?): Boolean {
        return true
    }

    @OnlyIn(Dist.CLIENT)
    override fun getShadeBrightness(state: BlockState?, level: IBlockReader?, pos: BlockPos?): Float {
        return 1.0f
    }

    override fun hasTileEntity(state: BlockState?): Boolean {
        return true
    }

    override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
        return MultiCoreBlockEntity()
    }
}