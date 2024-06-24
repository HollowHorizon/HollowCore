package ru.hollowhorizon.hc.common.containers

import kotlinx.serialization.Serializable
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.api.ICapabilityDispatcher
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

@Serializable
@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
class SyncEntityContainerPacket(
    private val entityId: Int, val capability: String, private val fromId: Int, private val toId: Int,
    val id: Int, private val leftButton: Boolean, private val hasShift: Boolean,
) : HollowPacketV3<SyncEntityContainerPacket> {
    override fun handle(player: Player) {
        val serverPlayer = player as ServerPlayer

        val from = getContainer(serverPlayer, entityId, capability, fromId)
        val to = getContainer(serverPlayer, entityId, capability, toId)


        ServerContainerManager.clickSlot(from, to, id, leftButton, hasShift)

        from.setChanged()
        to.setChanged()
    }

    private fun getContainer(player: ServerPlayer, entityId: Int, capability: String, id: Int): Container {
        if (id == -1) return player.inventory

        val entity = player.serverLevel().getEntity(entityId)
        val cap = (entity as ICapabilityDispatcher).capabilities.first { it.javaClass.name == capability }

        if (cap.containers.size <= id) {
            player.connection.disconnect("Invalid inventory operation!".mcText)
        }

        return cap.containers[id]
    }
}