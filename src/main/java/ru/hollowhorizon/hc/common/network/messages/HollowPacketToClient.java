package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.registry.HollowPacketProcessor;

import java.util.function.Supplier;

public class HollowPacketToClient {
    private final String name;

    public HollowPacketToClient(String dialogue) {
        this.name = dialogue;
    }

    public static HollowPacketToClient decode(PacketBuffer buf) {
        return new HollowPacketToClient(buf.readUtf());
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
    }

    public static void onReceived(HollowPacketToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        HollowPacketProcessor.getInstance(message.name).getInstance().process();
    }
}
