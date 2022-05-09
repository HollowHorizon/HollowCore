package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.function.Supplier;

public class StartStoryEventToClient {
    private final String name;

    public StartStoryEventToClient(String dialogue) {
        this.name = dialogue;
    }

    public static StartStoryEventToClient decode(PacketBuffer buf) {
        return new StartStoryEventToClient(buf.readUtf());
    }

    public static void onReceived(StartStoryEventToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        StoryEventStarter.start(message.name);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
    }
}
