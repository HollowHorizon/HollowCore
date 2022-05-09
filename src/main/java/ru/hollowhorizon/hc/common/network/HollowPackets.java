package ru.hollowhorizon.hc.common.network;

import net.minecraft.entity.player.PlayerEntity;
import ru.hollowhorizon.hc.api.registy.HollowPacket;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;

public class HollowPackets {
    @HollowPacket
    public static final UniversalPacket<HollowCapability<?>> SKILL_PANEL_PACKET = new UniversalPacket<HollowCapability<?>>() {

        @Override
        public HollowNBTSerializer<HollowCapability<?>> serializer() {
            return NBTUtils.HOLLOW_CAPABILITY_SERIALIZER;
        }

        @Override
        public void onReceived(PlayerEntity playerEntity, HollowCapability<?> value) {
            playerEntity.getCapability(value.getCapability()).ifPresent(capability -> capability.readNBT(value.writeNBT()));
        }
    };
}
