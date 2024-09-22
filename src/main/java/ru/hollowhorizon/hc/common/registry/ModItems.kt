package ru.hollowhorizon.hc.common.registry

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import ru.hollowhorizon.hc.client.utils.literal

object ModItems : HollowRegistry() {
    val JOKE by register("joke", AutoModelType.DEFAULT) {
        Example()
    }
}

class Example: Item(Properties()) {
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val loc = (player.pick(5.0, 0f, false) as BlockHitResult).blockPos
        if(level.isClientSide && usedHand == InteractionHand.MAIN_HAND) player.sendSystemMessage(ModMultiblocks.mithrilineFurnace.get().isValid(level, loc).toString().literal)
        return super.use(level, player, usedHand)
    }
}