package ru.hollowhorizon.hc.common.registry;

import net.minecraftforge.network.NetworkDirection;
import ru.hollowhorizon.hc.api.utils.HollowPacketInstance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class HollowPacketProcessor {
    private static final Map<String, PacketPackage> data = new HashMap<>();

    public static void process(Field field, String path, NetworkDirection direction) {
        if (Modifier.isStatic(field.getModifiers())) {
            try {
                Object someObject = field.get(null);
                if (someObject instanceof HollowPacketInstance) {
                    HollowPacketInstance packet = (HollowPacketInstance) someObject;

                    data.put(path, new PacketPackage(packet, direction));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static PacketPackage getInstance(String name) {
        return data.get(name);
    }

    public static String getName(HollowPacketInstance instance) {
        for(Map.Entry<String, PacketPackage> packet : data.entrySet()) {
            if(packet.getValue().getInstance() == instance) {
                return packet.getKey();
            }
        }
        return null;
    }

    public static PacketPackage getPackage(HollowPacketInstance instance) {
        for(Map.Entry<String, PacketPackage> packet : data.entrySet()) {
            if(packet.getValue().getInstance() == instance) {
                return packet.getValue();
            }
        }
        return null;
    }

    public static class PacketPackage {
        private final HollowPacketInstance instance;
        private final NetworkDirection direction;

        public PacketPackage(HollowPacketInstance instance, NetworkDirection direction) {
            this.direction = direction;
            this.instance = instance;
        }

        public HollowPacketInstance getInstance() {
            return instance;
        }

        public NetworkDirection getDirection() {
            return direction;
        }
    }
}
