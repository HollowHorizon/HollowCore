package ru.hollowhorizon.hc.common.multiblock

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import ru.hollowhorizon.hc.client.utils.literal
import ru.hollowhorizon.hc.common.registry.HollowRegistry
import ru.hollowhorizon.hc.common.registry.ModMultiblocks

class Example : Block(Properties.of(Material.STONE)), IMultiBlock {
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        player.sendSystemMessage(ModMultiblocks.mithrilineFurnace.get().isValid(level, pos).toString().literal)
        return super.use(state, level, pos, player, hand, hit)
    }
}

object Reg: HollowRegistry() {
    val example by register("example") { Example() }
}