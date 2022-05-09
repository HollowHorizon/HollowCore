package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.network.UniversalPacket;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.client.utils.NBTUtils;
import ru.hollowhorizon.hc.common.network.UniversalPacketManager;

import java.util.function.Supplier;

public class HollowPacketToServer {
    private final UniversalPacket<?> packet;
    private final String packetName;

    public HollowPacketToServer(UniversalPacket<?> packet, String packetName) {
        this.packet = packet;
        this.packetName = packetName;
    }

    public static HollowPacketToServer decode(PacketBuffer buf) {
        String name = buf.readUtf();
        String serializer = buf.readUtf();
        return new HollowPacketToServer(UniversalPacketManager.getPacketFromNBT(buf.readNbt().getCompound("value"), serializer, name), name);
    }

    public static <T> void onReceived(HollowPacketToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        message.packet.process(ctxSupplier.get().getSender());
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(packetName);
        buf.writeUtf(NBTUtils.getName(packet.serializer()));
        NBTUtils.saveValue(buf, "value", packet.value, (HollowNBTSerializer) packet.serializer());
    }
}
