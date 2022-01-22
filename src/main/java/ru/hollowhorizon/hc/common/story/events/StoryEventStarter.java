package ru.hollowhorizon.hc.common.story.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.network.data.StoryInfoData;

public class StoryEventStarter {
    public static void startAll(ServerPlayerEntity player) {
        String[] arr = StoryInfoData.INSTANCE.getAll(player);
        if (arr != null) {
            for (String name : arr) {
                if (!StoryEventListener.hasLore(name, player)) StoryEventListener.startLoreNoUpdate(name, player);
            }
        }
    }

    public static void stopAll(ServerPlayerEntity player) {
        StoryEventListener.stopAll(player);
    }

    public static void start(ServerPlayerEntity player, String registryName) {
        StoryEventListener.startLore(registryName, player);
    }
}
