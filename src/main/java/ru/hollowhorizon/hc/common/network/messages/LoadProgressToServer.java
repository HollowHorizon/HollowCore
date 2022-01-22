package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.handlers.SaveStoryHandler;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.function.Supplier;

public class LoadProgressToServer {
    private final int slot;

    public LoadProgressToServer(int readInt) {
        this.slot = readInt;
    }

    public static LoadProgressToServer decode(PacketBuffer buf) {
        return new LoadProgressToServer(buf.readInt());
    }

    public void encode(PacketBuffer buf) {
        buf.writeInt(slot);
    }

    public static void onReceived(LoadProgressToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        SaveStoryHandler.loadStory(ctxSupplier.get().getSender(), message.slot);

        StoryEventStarter.stopAll(ctxSupplier.get().getSender());
        StoryEventStarter.startAll(ctxSupplier.get().getSender());
    }

}
