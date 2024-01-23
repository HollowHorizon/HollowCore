package ru.hollowhorizon.hc.client.utils;

import org.jetbrains.annotations.NotNull;
import ru.hollowhorizon.hc.common.network.HollowPacketV2Kt;
import ru.hollowhorizon.hc.common.network.HollowPacketV3Kt;

public class JavaHacks {
    public static <R, K> K forceCast(R original) {
        return (K) original;
    }

    public static void registerPacket(Class<?> packet, @NotNull String modId) {
        HollowPacketV3Kt.registerPacket(forceCast(packet), modId);
    }


}
