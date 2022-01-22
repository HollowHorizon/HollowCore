package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.handlers.SaveStoryHandler;

import java.util.function.Supplier;

public class SaveProgressToServer {
    public final int slot;

    public SaveProgressToServer(int slot) {
        this.slot = slot;
    }

    public static SaveProgressToServer decode(PacketBuffer buf) {
        return new SaveProgressToServer(buf.readInt());
    }

    public static void onReceived(SaveProgressToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        SaveStoryHandler.saveStory(ctxSupplier.get().getSender(), message.slot);
    }

    public void encode(PacketBuffer buf) {
        buf.writeInt(slot);
    }
}
