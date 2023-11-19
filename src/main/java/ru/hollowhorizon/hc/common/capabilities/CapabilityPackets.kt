package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.nbt.ForTag
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet
import ru.hollowhorizon.hc.common.network.send

@Serializable
data class LevelCapabilityContainer(
    val level: String,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
)

@Serializable
data class EntityCapabilityContainer(
    val entityId: Int,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
)

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_CLIENT)
class CSyncEntityCapabilityPacket : Packet<EntityCapabilityContainer>({ player, container ->
    val entity = player.level.getEntity(container.entityId)
        ?: throw IllegalStateException("Entity with id ${container.entityId} not found: $container".apply(HollowCore.LOGGER::warn))
    val cap = entity.getCapability(CapabilityStorage.storages[container.capability] as Capability<CapabilityInstance>)
        .orElseThrow { IllegalStateException("Unknown capability: $container".apply(HollowCore.LOGGER::warn)) }

    if((container.value as? CompoundTag)?.isEmpty == false) {
        cap.deserializeNBT(container.value)
    }
})

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_SERVER)
class SSyncEntityCapabilityPacket : Packet<EntityCapabilityContainer>({ player, container ->
    val entity = player.level.getEntity(container.entityId)
        ?: throw IllegalStateException("Entity with id ${container.entityId} not found: $container".apply(HollowCore.LOGGER::warn))
    val cap = entity.getCapability(CapabilityStorage.storages[container.capability] as Capability<CapabilityInstance>)
        .orElseThrow { IllegalStateException("Unknown capability: $container".apply(HollowCore.LOGGER::warn)) }

    if (cap.consumeOnServer) {
        cap.deserializeNBT(container.value)
        CSyncEntityCapabilityPacket().send(container, PacketDistributor.TRACKING_ENTITY.with { entity })
    }
})

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_CLIENT)
class CSyncLevelCapabilityPacket : Packet<LevelCapabilityContainer>({ player, container ->
    val level = player.level
    val cap = level.getCapability(CapabilityStorage.storages[container.capability] as Capability<CapabilityInstance>)
        .orElseThrow { IllegalStateException("Unknown capability: $container".apply(HollowCore.LOGGER::warn)) }
    cap.deserializeNBT(container.value)
})

@HollowPacketV2(toTarget = NetworkDirection.PLAY_TO_SERVER)
class SSyncLevelCapabilityPacket : Packet<LevelCapabilityContainer>({ player, container ->
    val server = player.server ?: throw IllegalStateException("Server not found".apply(HollowCore.LOGGER::warn))
    val levelKey = server.levelKeys().find { it.location().equals(container.level.rl) }
        ?: throw IllegalStateException("Unknown level: $container".apply(HollowCore.LOGGER::warn))
    val level = server.getLevel(levelKey) ?: throw IllegalStateException("Level not found: $container".apply(HollowCore.LOGGER::warn))
    val cap = level.getCapability(CapabilityStorage.storages[container.capability] as Capability<CapabilityInstance>)
        .orElseThrow { IllegalStateException("Unknown capability: $container".apply(HollowCore.LOGGER::warn)) }

    if (cap.consumeOnServer) {
        cap.deserializeNBT(container.value)
        CSyncLevelCapabilityPacket().send(container, PacketDistributor.DIMENSION.with { player.level.dimension() })
    }
})

