package ru.hollowhorizon.hc.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.hollowhorizon.hc.common.network.packets.SpawnParticlesPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandler {
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final ResourceLocation HOLLOW_CORE_CHANNEL = new ResourceLocation("hc", "hollow_core_channel");
    public static final Map<String, List<Runnable>> PACKETS = new HashMap<>();
    public static SimpleChannel HollowCoreChannel;
    public static int PACKET_INDEX = 0;

    public static <MSG> void sendMessageToClient(MSG messageToClient, Player player) {
        HollowCoreChannel.sendTo(messageToClient, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendMessageToClientTrackingChunk(MSG msg, Level level, BlockPos pos) {
        HollowCoreChannel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), msg);
    }

    @OnlyIn(Dist.CLIENT)
    public static <MSG> void sendMessageToServer(MSG messageToServer) {
        if (Minecraft.getInstance().getConnection() == null) return;
        HollowCoreChannel.sendToServer(messageToServer);
    }

    public static void register() {
        if (HollowCoreChannel == null) {
            HollowCoreChannel = NetworkRegistry.newSimpleChannel(HOLLOW_CORE_CHANNEL, () -> MESSAGE_PROTOCOL_VERSION,
                    MESSAGE_PROTOCOL_VERSION::equals,
                    MESSAGE_PROTOCOL_VERSION::equals
            );
        }

        //без сортировки он может это сделать в любом порядке тем самым поломав сетевую игру
        PACKETS.keySet().stream().sorted().forEach(it ->
                PACKETS.get(it).forEach(Runnable::run)
        );

        HollowCoreChannel.registerMessage(PACKET_INDEX++, SpawnParticlesPacket.class, SpawnParticlesPacket::write, SpawnParticlesPacket::read, SpawnParticlesPacket::handle);
    }
}
