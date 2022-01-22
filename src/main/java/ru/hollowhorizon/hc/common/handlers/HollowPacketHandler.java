package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import ru.hollowhorizon.hc.api.utils.HollowPacketInstance;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.HollowPacketToClient;
import ru.hollowhorizon.hc.common.network.messages.HollowPacketToServer;
import ru.hollowhorizon.hc.common.registry.HollowPacketProcessor;

public class HollowPacketHandler {
    public static void sendPacket(HollowPacketInstance packet, PlayerEntity player) {
        HollowPacketProcessor.PacketPackage pack = HollowPacketProcessor.getPackage(packet);

        if (pack == null) return;

        if (pack.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            NetworkHandler.sendMessageToClient(new HollowPacketToClient(HollowPacketProcessor.getName(packet)), player);
        } else {
            NetworkHandler.sendMessageToServer(new HollowPacketToServer(HollowPacketProcessor.getName(packet)));
        }
    }
}
