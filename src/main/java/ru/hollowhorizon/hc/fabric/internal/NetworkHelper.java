//? if fabric {
package ru.hollowhorizon.hc.fabric.internal;

import kotlin.Unit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.network.HollowPacketV3Kt;

public class NetworkHelper {
    public static void register() {
        HollowPacketV3Kt.registerPacket = (type) -> {
            FabricNetworkKt.registerPacket(JavaHacks.forceCast(type));
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToClient = (player, hollowPacketV3) -> {
            ServerPlayNetworking.send(player, hollowPacketV3);
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToServer = (hollowPacketV3) -> {
            ClientPlayNetworking.send(hollowPacketV3);
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.registerPackets.invoke();
    }
}
//?}