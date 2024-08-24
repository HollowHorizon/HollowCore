package ru.hollowhorizon.hc.forge.internal;

import kotlin.Unit;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.network.HollowPacketV3Kt;

public class ForgeNetworkHelper {
    public static SimpleChannel hollowCoreChannel = ChannelBuilder.named("hollowcore:hollow_packets")
            .networkProtocolVersion(4)
            .clientAcceptedVersions(Channel.VersionTest.exact(4))
            .serverAcceptedVersions(Channel.VersionTest.exact(4))
            .simpleChannel();

    public static void register() {
        HollowPacketV3Kt.registerPacket = (type) -> {
            ForgeNetworkKt.registerPacket(JavaHacks.forceCast(type));
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToClient = (player, hollowPacketV3) -> {
            hollowCoreChannel.send(hollowPacketV3, PacketDistributor.PLAYER.with(player));
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.sendPacketToServer = (hollowPacketV3) -> {
            hollowCoreChannel.send(hollowPacketV3, PacketDistributor.SERVER.noArg());
            return Unit.INSTANCE;
        };
        HollowPacketV3Kt.registerPackets.invoke();
    }
}
