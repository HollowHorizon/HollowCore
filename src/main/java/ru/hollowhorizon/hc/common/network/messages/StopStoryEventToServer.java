package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.function.Supplier;

public class StopStoryEventToServer {
    private final String name;

    public StopStoryEventToServer(String dialogue) {
        this.name = dialogue;
    }

    public static StopStoryEventToServer decode(PacketBuffer buf) {
        return new StopStoryEventToServer(buf.readUtf());
    }

    public static void onReceived(StopStoryEventToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        StoryEventStarter.end(ctxSupplier.get().getSender(), message.name);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
    }
}
