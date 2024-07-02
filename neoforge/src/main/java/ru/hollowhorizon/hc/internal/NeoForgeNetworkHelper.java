package ru.hollowhorizon.hc.internal;

import kotlin.Unit;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.network.HollowPacketV3Kt;

public class NeoForgeNetworkHelper {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        HollowPacketV3Kt.registerPacket = (type) -> {
            NeoForgeNetworkKt.registerPacket(registrar, JavaHacks.forceCast(type));
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToClient = (player, hollowPacketV3) -> {
            PacketDistributor.sendToPlayer(player, hollowPacketV3);
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToServer = (hollowPacketV3) -> {
            PacketDistributor.sendToServer(hollowPacketV3);
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.registerPackets.invoke();
    }
}
