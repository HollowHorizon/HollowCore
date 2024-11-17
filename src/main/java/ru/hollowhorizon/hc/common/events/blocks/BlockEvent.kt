package ru.hollowhorizon.hc.common.events.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import ru.hollowhorizon.hc.common.events.Cancelable
import ru.hollowhorizon.hc.common.events.Event
import java.util.*

open class BlockEvent(var state: BlockState, val pos: BlockPos) : Event, Cancelable {
    override var isCanceled = false

    class NeighborNotify(
        state: BlockState,
        pos: BlockPos,
        val directions: EnumSet<Direction>,
    ): BlockEvent(state, pos)

    class Remove(
        val level: Level,
        state: BlockState,
        pos: BlockPos,
        val isMoving: Boolean
    ): BlockEvent(state, pos)
}