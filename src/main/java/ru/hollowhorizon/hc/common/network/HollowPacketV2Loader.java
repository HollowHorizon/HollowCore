package ru.hollowhorizon.hc.common.network;


public class HollowPacketV2Loader {

    @SuppressWarnings("unchecked")
    public static void register(Packet<?> packet) {
        NetworkHandler.PACKET_TASKS.add(() -> NetworkHandler.HollowCoreChannel.registerMessage(
                NetworkHandler.PACKET_INDEX++, (Class<Packet<?>>) packet.getClass(),
                packet::encode, packet::decode, packet::onReceive
        ));
    }
}
