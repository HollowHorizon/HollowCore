package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.api.registy.StoryObject;

@StoryObject
public class ExampleAnimation extends CutsceneHandler {

    @Override
    public void start(ServerPlayerEntity player) {

        moveTo(player.blockPosition(), player.blockPosition().offset(233, 5, 130), 2000);

        super.start(player);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public String getName() {
        return "TochnoNe.NewGenStory.GectorAndDrBlackCutscene";
    }

}
