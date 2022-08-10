package ru.hollowhorizon.hc.common.story.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;

public class StoryEventStarter {
    public static void startAll(ServerPlayerEntity player) {
        player.getCapability(ModCapabilities.STORY_CAPABILITY).ifPresent((capability) -> {
            for (String storyName : capability.getAll()) {
                start(player, storyName);
            }
        });
    }

    public static void stopAll(ServerPlayerEntity player) {
        StoryEventListener.stopAll(player);
    }

    public static void start(PlayerEntity player, String registryName) {
        StoryEventListener.startLore(registryName, player);
    }

    public static void end(PlayerEntity player, String s) {
        StoryEventListener.endStory(s, player);
    }

    @OnlyIn(Dist.CLIENT)
    public static void start(String name) {
        start(Minecraft.getInstance().player, name);
    }

    @OnlyIn(Dist.CLIENT)
    public static void end(String name) {
        end(Minecraft.getInstance().player, name);
    }
}
