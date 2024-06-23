package ru.hollowhorizon.hc.common.containers

import kotlinx.serialization.Serializable
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

enum class ContainerAction {
    MOVE_ITEM, SWAP_ITEMS, THROW_ITEM, QUICK_MOVE_ITEM
}

@Serializable
@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
class ContainerPacket(val action: ContainerAction, val fromSlot: Int, val toSlot: Int) :
    HollowPacketV3<ContainerPacket> {
    override fun handle(player: Player) {
        val serverPlayer = player as ServerPlayer
        val container = serverPlayer.inventory

        when (action) {
            ContainerAction.MOVE_ITEM -> {
                val slot = container.getItem(toSlot)

                if (!slot.isEmpty) {
                    HollowCore.LOGGER.warn("Player ${player.name} trying move item to slot $toSlot. It busy with ${slot.displayName.string}.")
                    container.setChanged()
                    return
                }

                val fromItem = container.getItem(fromSlot)
                container.setItem(toSlot, fromItem)
                container.setItem(fromSlot, ItemStack.EMPTY)
            }

            else -> throw UnsupportedOperationException("$action action not supported!")
        }

        container.setChanged()
    }

}