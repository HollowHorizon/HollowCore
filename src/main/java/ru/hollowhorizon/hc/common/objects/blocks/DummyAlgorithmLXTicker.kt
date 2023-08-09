package ru.hollowhorizon.hc.common.objects.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class BE: EntityBlock {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity? {
        TODO("Not yet implemented")
    }

    override fun <T : BlockEntity> getTicker(
        pLevel: Level,
        pState: BlockState,
        pBlockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker { level: Level, pos: BlockPos, state: BlockState, entity: T ->

        }
    }
}