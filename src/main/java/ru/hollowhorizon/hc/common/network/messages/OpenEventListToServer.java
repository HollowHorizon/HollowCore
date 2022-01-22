package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenEventListToServer {

    public static OpenEventListToServer decode(PacketBuffer buf) {
        return new OpenEventListToServer();
    }


    public void encode(PacketBuffer buf) {

    }

    public static void onReceived(OpenEventListToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);
        List<String> names = new ArrayList<>();
        for (HollowStoryHandler storyHandler : StoryEventListener.activeLore) {
            if (storyHandler.player.equals(ctxSupplier.get().getSender())) {
                names.add(storyHandler.getStoryName());
            }
        }
        NetworkHandler.sendMessageToClient(new OpenEventListToClient(names), ctxSupplier.get().getSender());
    }
}
