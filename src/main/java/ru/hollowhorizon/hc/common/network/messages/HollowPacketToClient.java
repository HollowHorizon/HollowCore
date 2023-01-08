package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.network.UniversalPacket;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.network.UniversalPacketManager;

import java.util.function.Supplier;

public class HollowPacketToClient {
    private final UniversalPacket<?> packet;
    private final String name;

    public <T extends UniversalPacket<?>> HollowPacketToClient(T packet, String name) {
        this.packet = packet;
        this.name = name;
    }

    public static HollowPacketToClient decode(PacketBuffer buf) {
        String name = buf.readUtf();
        String serializer = buf.readUtf();
        return new HollowPacketToClient(UniversalPacketManager.getPacketFromNBT(buf.readNbt().getCompound("value"), serializer, name), name);
    }

    public static void onReceived(HollowPacketToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        message.packet.process();
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(name);
        buf.writeUtf(NBTUtils.getName(packet.serializer()));
        NBTUtils.saveValue(buf, "value", packet.value, (HollowNBTSerializer) packet.serializer());
    }
}
