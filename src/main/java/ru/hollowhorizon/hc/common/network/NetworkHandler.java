package ru.hollowhorizon.hc.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.hollowhorizon.hc.HollowCore;

import java.util.ArrayList;
import java.util.List;

public class NetworkHandler {
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final ResourceLocation HOLLOW_CORE_CHANNEL = new ResourceLocation("hc", "hollow_core_channel");
    public static final List<Runnable> PACKET_TASKS = new ArrayList<>();
    public static SimpleChannel HollowCoreChannel;
    public static int PACKET_INDEX = 0;

    public static <MSG> void sendMessageToClient(MSG messageToClient, Player player) {
        HollowCore.LOGGER.info("Sending packet to client: {}", messageToClient);
        HollowCoreChannel.sendTo(messageToClient, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static <MSG> void sendMessageToServer(MSG messageToServer) {
        if (Minecraft.getInstance().getConnection() == null) return;
        HollowCore.LOGGER.info("Sending packet to server: {}", messageToServer);
        HollowCoreChannel.sendToServer(messageToServer);
    }

    public static void register() {
        if (HollowCoreChannel == null) {
            HollowCoreChannel = NetworkRegistry.newSimpleChannel(HOLLOW_CORE_CHANNEL, () -> MESSAGE_PROTOCOL_VERSION,
                    MESSAGE_PROTOCOL_VERSION::equals,
                    MESSAGE_PROTOCOL_VERSION::equals
            );
        }

        PACKET_TASKS.forEach(Runnable::run);
    }
}
