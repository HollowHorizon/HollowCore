package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.client.screens.EventListScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenEventListToClient {
    private final List<String> names;

    public OpenEventListToClient(List<String> list) {
        this.names = list;
    }

    public static OpenEventListToClient decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(buf.readUtf());
        }

        return new OpenEventListToClient(list);
    }

    public static void onReceived(OpenEventListToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        ctxSupplier.get().enqueueWork(() -> EventListScreen.open(message.names));
    }

    public void encode(PacketBuffer buf) {


        buf.writeInt(names.size());

        for (String story : names) {
            buf.writeUtf(story);
        }
    }
}