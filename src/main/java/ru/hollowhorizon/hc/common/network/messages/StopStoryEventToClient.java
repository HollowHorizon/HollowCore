package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.function.Supplier;

public class StopStoryEventToClient {
    private final String name;

    public StopStoryEventToClient(String dialogue) {
        this.name = dialogue;
    }

    public static StopStoryEventToClient decode(PacketBuffer buf) {
        return new StopStoryEventToClient(buf.readUtf());
    }

    public static void onReceived(StopStoryEventToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        StoryEventStarter.end(message.name);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
    }
}
