package ru.hollowhorizon.hc.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;
import ru.hollowhorizon.hc.common.network.messages.HollowPacketToClient;
import ru.hollowhorizon.hc.common.network.messages.HollowPacketToServer;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.NBTUtils;

import java.util.HashMap;
import java.util.Map;

public class UniversalPacketManager {
    public static final Map<String, UniversalPacket<?>> PACKETS = new HashMap<>();

    public static UniversalPacket<?> getPacketFromNBT(CompoundNBT nbt, String serializer, String name) {
        UniversalPacket<?> packet =  PACKETS.get(name);
        HollowNBTSerializer<?> hollowNBTSerializer = NBTUtils.SERIALIZERS.get(new ResourceLocation(serializer));
        if(serializer.equals("hc:none")) return packet;
        try {
            packet.value = HollowJavaUtils.castDarkMagic(hollowNBTSerializer.fromNBT(nbt));
        } catch (Exception e) {
            throw new IllegalStateException("Packet "+name+" not found. Check your @HollowPacket annotation");
        }
        return packet;
    }

    public static <T extends UniversalPacket<?>> String getName(T packet) {
        for (Map.Entry<String, UniversalPacket<?>> ser : PACKETS.entrySet()) {
            if (ser.getValue().equals(packet)) {
                return ser.getKey();
            }
        }
        return "null";
    }

    public static <T extends UniversalPacket<?>> void sendToClient(ServerPlayerEntity playerEntity, T packet) {
        NetworkHandler.sendMessageToClient(new HollowPacketToClient(packet, getName(packet)), playerEntity);
    }

    public static <T extends UniversalPacket<?>> void sendToServer(T packet) {
        NetworkHandler.sendMessageToServer(new HollowPacketToServer(packet, getName(packet)));
    }
}
