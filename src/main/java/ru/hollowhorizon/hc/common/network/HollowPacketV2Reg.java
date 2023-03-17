package ru.hollowhorizon.hc.common.network;

import java.util.ArrayList;
import java.util.List;

public class HollowPacketV2Reg {
    public static final List<Packet<?>> PLAYER_PACKETS = new ArrayList<>();
    public static final List<Packet<?>> ENTITY_PACKETS = new ArrayList<>();
    public static final List<Packet<?>> LEVEL_PACKETS = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void registerAll() {
        PLAYER_PACKETS.forEach(packet -> NetworkHandler.HollowCoreChannel.registerMessage(NetworkHandler.index++,
                (Class<Packet<?>>) packet.getClass(),
                packet::encode,
                packet::decode,
                packet::onReceive
        ));

        ENTITY_PACKETS.forEach(packet -> NetworkHandler.HollowCoreChannel.registerMessage(NetworkHandler.index++,
                (Class<Packet<?>>) packet.getClass(),
                packet::encode,
                packet::decode,
                packet::onReceive
        ));

        LEVEL_PACKETS.forEach(packet -> NetworkHandler.HollowCoreChannel.registerMessage(NetworkHandler.index++,
                (Class<Packet<?>>) packet.getClass(),
                packet::encode,
                packet::decode,
                packet::onReceive
        ));

    }
}
