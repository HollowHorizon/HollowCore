package ru.hollowhorizon.hc.common.network;

import java.util.ArrayList;
import java.util.List;

public class HollowPacketV2Reg {
    public static final List<Packet<?>> PACKETS = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void registerAll() {
        for(Packet<?> packet : PACKETS) {
            NetworkHandler.HollowCoreChannel.registerMessage(NetworkHandler.index++,
                    (Class<Packet<?>>) packet.getClass(),
                    packet::encode,
                    packet::decode,
                    packet::onReceive
            );
        }

    }
}
