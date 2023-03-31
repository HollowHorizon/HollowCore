package ru.hollowhorizon.hc.common.story;

import net.minecraft.entity.player.PlayerEntity;
import ru.hollowhorizon.hc.api.registy.StoryObject;

@StoryObject
public class StoryEventExample extends HollowStoryHandler {
    @Override
    public void start(PlayerEntity player) {
        super.start(player);
    }

    @Override
    public String getStoryName() {
        return "ttt";
    }
}
