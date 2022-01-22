package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.registry.HollowPacketProcessor;

import java.util.function.Supplier;

public class HollowPacketToServer {
    private final String name;

    public HollowPacketToServer(String dialogue) {
        this.name = dialogue;
    }

    public static HollowPacketToServer decode(PacketBuffer buf) {
        return new HollowPacketToServer(buf.readUtf());
    }

    public static void onReceived(HollowPacketToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        HollowPacketProcessor.getInstance(message.name).getInstance().process();
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
    }
}
