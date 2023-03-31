package ru.hollowhorizon.hc.common.network.messages

import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.capabilities.HollowCapability
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater
import ru.hollowhorizon.hc.common.capabilities.serialize
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet

@HollowPacketV2
class SyncCapabilityPlayer : Packet<HollowCapability>({ player, capability ->
    //Данная проверка нужна, чтобы Capability нельзя было изменить на клиенте и отправить на сервер
    //Зачастую это стало бы уязвимостью, а так со стороны сервера такие изменения будут отбрасываться
    if (capability.consumeDataFromClient || player.level.isClientSide) {
        val cap = HollowCapabilityV2.get(capability.javaClass)

        val capabilityUpdater = player as ICapabilityUpdater

        capabilityUpdater.updateCapability(cap, capability.serialize())
    }
})

@HollowPacketV2
class SyncCapabilityWorld : Packet<HollowCapability>({ player, capability ->
    //Данная проверка нужна, чтобы Capability нельзя было изменить на клиенте и отправить на сервер
    //Зачастую это стало бы уязвимостью, а так со стороны сервера такие изменения будут отбрасываться
    if (capability.consumeDataFromClient || player.level.isClientSide) {
        val cap = HollowCapabilityV2.get(capability.javaClass)

        val uploader = Minecraft.getInstance().level as ICapabilityUpdater

        uploader.updateCapability(cap, capability.serialize())
    }
})

@HollowPacketV2
class SyncCapabilityEntity : Packet<CapabilityForEntity>({ player, data ->
    data.apply {
        //Данная проверка нужна, чтобы Capability нельзя было изменить на клиенте и отправить на сервер
        //Зачастую это стало бы уязвимостью, а так со стороны сервера такие изменения будут отбрасываться
        if(capability.consumeDataFromClient || player.level.isClientSide) {
            val cap = HollowCapabilityV2.get(capability.javaClass)

            val entityId = data.entityId

            val mob = player.level.getEntity(entityId)!!
            val uploader = mob as ICapabilityUpdater

            HollowCore.LOGGER.info("Updating EntityCapability: {}, {}", uploader, capability.serialize().asString)

            uploader.updateCapability(cap, capability.serialize())
        }
    }
})

@Serializable
data class CapabilityForEntity(val capability: HollowCapability, val entityId: Int)