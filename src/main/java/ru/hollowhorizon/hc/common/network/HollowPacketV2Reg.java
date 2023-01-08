package ru.hollowhorizon.hc.common.network;

public class HollowPacketV2Reg {
    @SuppressWarnings("unchecked")
    public static void register(Packet<?> packet) {

        NetworkHandler.HollowCoreChannel.registerMessage(NetworkHandler.index++,
                (Class<Packet<?>>) packet.getClass(),
                (packet1, packetBuffer) -> packet.encode(packet, packetBuffer),
                packet::decode,
                (packet1, context) -> packet.onReceive(packet, context)
        );

    }
}
