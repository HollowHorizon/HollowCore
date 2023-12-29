package ru.hollowhorizon.hc.common.network;


import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

public class HollowPacketV2Loader {

    @SuppressWarnings("unchecked")
    public static void register(Packet<?> packet, NetworkDirection direction) {
        NetworkHandler.PACKET_TASKS.add(() ->
                NetworkHandler.HollowCoreChannel.registerMessage(
                        NetworkHandler.PACKET_INDEX++, (Class<Packet<?>>) packet.getClass(),
                        packet::encode, packet::decode, packet::onReceive, Optional.of(direction)
                )
        );
    }
}
