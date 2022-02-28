package ru.hollowhorizon.hc.common.story;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import ru.hollowhorizon.hc.common.network.data.StoryInfoData;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;

public abstract class HollowStoryHandler {
    public ServerPlayerEntity player;
    public void start(ServerPlayerEntity player) {
        MinecraftForge.EVENT_BUS.register(this);
        this.player = player;
    }

    public void stop() {
        StoryInfoData.INSTANCE.removeData(this.player, getStoryName());
        MinecraftForge.EVENT_BUS.unregister(this);
        StoryEventListener.stopStory(getStoryName(), player);
    }

    public abstract String getStoryName();
}
