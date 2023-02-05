package ru.hollowhorizon.hc.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import ru.hollowhorizon.hc.common.network.messages.*;

import java.util.Optional;

public class NetworkHandler {
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final ResourceLocation HOLLOW_CORE_CHANNEL = new ResourceLocation("hc", "hollow_core_channel");
    public static SimpleChannel HollowCoreChannel;
    public static int index = 7;

    public static <MSG> void sendMessageToClient(MSG messageToClient, PlayerEntity player) {
        HollowCoreChannel.sendTo(messageToClient, ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendMessageToServer(MSG messageToServer) {
        HollowCoreChannel.sendToServer(messageToServer);
    }

    public static void register() {
        if (HollowCoreChannel == null) {
            HollowCoreChannel = NetworkRegistry.newSimpleChannel(HOLLOW_CORE_CHANNEL, () -> MESSAGE_PROTOCOL_VERSION,
                    MESSAGE_PROTOCOL_VERSION::equals,
                    MESSAGE_PROTOCOL_VERSION::equals
            );
        }

        int i = 0;

        HollowCoreChannel.registerMessage(i++,
                HollowPacketToClient.class,
                HollowPacketToClient::encode,
                HollowPacketToClient::decode,
                HollowPacketToClient::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        HollowCoreChannel.registerMessage(i++,
                HollowPacketToServer.class,
                HollowPacketToServer::encode,
                HollowPacketToServer::decode,
                HollowPacketToServer::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        HollowCoreChannel.registerMessage(i++,
                UpdateStoryEventToServer.class,
                UpdateStoryEventToServer::encode,
                UpdateStoryEventToServer::decode,
                UpdateStoryEventToServer::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        HollowCoreChannel.registerMessage(i++,
                UpdateStoryEventToClient.class,
                UpdateStoryEventToClient::encode,
                UpdateStoryEventToClient::decode,
                UpdateStoryEventToClient::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        HollowCoreChannel.registerMessage(i++,
                StartStoryEventToClient.class,
                StartStoryEventToClient::encode,
                StartStoryEventToClient::decode,
                StartStoryEventToClient::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        HollowCoreChannel.registerMessage(i++,
                StopStoryEventToClient.class,
                StopStoryEventToClient::encode,
                StopStoryEventToClient::decode,
                StopStoryEventToClient::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        HollowCoreChannel.registerMessage(i,
                StartDialogueToClient.class,
                StartDialogueToClient::encode,
                StartDialogueToClient::decode,
                StartDialogueToClient::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        HollowPacketV2Reg.registerAll();
    }
}
