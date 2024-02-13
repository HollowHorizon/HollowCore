package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.ICapabilityDispatcher
import ru.hollowhorizon.hc.client.utils.nbt.ForTag
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CSyncEntityCapabilityPacket(
    val entityId: Int,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<CSyncEntityCapabilityPacket> {
    override fun handle(player: Player, data: CSyncEntityCapabilityPacket) {
        val entity = player.level.getEntity(data.entityId)
            ?: throw IllegalStateException("Entity with id ${data.entityId} not found: $data".apply(HollowCore.LOGGER::warn))
        val cap = entity.getCapability(CapabilityStorage.storages[data.capability] as Capability<CapabilityInstance>)
            .orElseThrow { IllegalStateException("Unknown capability: $data".apply(HollowCore.LOGGER::warn)) }

        if ((data.value as? CompoundTag)?.isEmpty == false) {
            cap.deserializeNBT(data.value)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class SSyncEntityCapabilityPacket(
    val entityId: Int,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<SSyncEntityCapabilityPacket> {
    override fun handle(player: Player, data: SSyncEntityCapabilityPacket) {
        val entity = player.level.getEntity(data.entityId)
            ?: throw IllegalStateException("Entity with id ${data.entityId} not found: $data".apply(HollowCore.LOGGER::warn))
        val cap = entity.getCapability(CapabilityStorage.storages[data.capability] as Capability<CapabilityInstance>)
            .orElseThrow { IllegalStateException("Unknown capability: $data".apply(HollowCore.LOGGER::warn)) }

        if (cap.consumeOnServer) {
            cap.deserializeNBT(data.value)
            CSyncEntityCapabilityPacket(
                data.entityId,
                data.capability,
                data.value
            ).send(PacketDistributor.TRACKING_ENTITY.with { entity })
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CSyncLevelCapabilityPacket(
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<CSyncLevelCapabilityPacket> {
    override fun handle(player: Player, data: CSyncLevelCapabilityPacket) {
        val level = player.level
        val cap = level.getCapability(CapabilityStorage.storages[data.capability] as Capability<CapabilityInstance>)
            .orElseThrow { IllegalStateException("Unknown capability: $data".apply(HollowCore.LOGGER::warn)) }
        cap.deserializeNBT(data.value)
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class SSyncLevelCapabilityPacket(
    val level: String,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<SSyncLevelCapabilityPacket> {
    override fun handle(player: Player, data: SSyncLevelCapabilityPacket) {
        val server = player.server ?: throw IllegalStateException("Server not found".apply(HollowCore.LOGGER::warn))
        val levelKey = server.levelKeys().find { it.location().equals(data.level.rl) }
            ?: throw IllegalStateException("Unknown level: $data".apply(HollowCore.LOGGER::warn))
        val level = server.getLevel(levelKey)
            ?: throw IllegalStateException("Level not found: $data".apply(HollowCore.LOGGER::warn))
        val cap = level.getCapability(CapabilityStorage.storages[data.capability] as Capability<CapabilityInstance>)
            .orElseThrow { IllegalStateException("Unknown capability: $data".apply(HollowCore.LOGGER::warn)) }

        if (cap.consumeOnServer) {
            cap.deserializeNBT(data.value)
            CSyncLevelCapabilityPacket(
                data.capability,
                data.value
            ).send(PacketDistributor.DIMENSION.with { player.level.dimension() })
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CSyncTeamCapabilityPacket(
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<CSyncTeamCapabilityPacket> {
    override fun handle(player: Player, data: CSyncTeamCapabilityPacket) {
        val updater = FTBTeamsAPI.getClientManager().selfTeam as ICapabilityProvider

        updater.getCapability(CapabilityStorage.storages[data.capability] as Capability<CapabilityInstance>)
            .orElseThrow { IllegalStateException("Unknown capability: $data".apply(HollowCore.LOGGER::warn)) }
            .deserializeNBT(value)
    }
}
