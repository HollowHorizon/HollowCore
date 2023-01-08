package ru.hollowhorizon.hc.common.story;

import net.minecraft.entity.player.PlayerEntity;
import ru.hollowhorizon.hc.api.registy.StoryObject;
import ru.hollowhorizon.hc.common.network.ExamplePacket;

@StoryObject
public class StoryEventExample extends HollowStoryHandler {
    @Override
    public void start(PlayerEntity player) {
        super.start(player);
        new ExamplePacket().send("data 1", player);
    }

    @Override
    public String getStoryName() {
        return "ttt";
    }
}
