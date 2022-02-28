package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.api.registy.StoryObject;
import ru.hollowhorizon.hc.api.utils.DelayedAction;
import ru.hollowhorizon.hc.common.events.action.HollowAction;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

@StoryObject
public class TestHandler extends HollowStoryHandler {
    private int roll = 180;
    private int yaw = 0;
    @DelayedAction
    private static final HollowAction action = (player -> StoryEventStarter.end(player, "test.test"));

    @Override
    public void start(ServerPlayerEntity player) {
        super.start(player);
        action.run(player, 200);
    }

    @SubscribeEvent
    public void onCameraEdit(EntityViewRenderEvent.CameraSetup setup) {

        setup.setYaw(yaw);
        if (roll < 360) roll += 1;
        else roll = 0;

        if (yaw < 360) yaw += 1;
        else yaw = 0;
    }

    @Override
    public String getStoryName() {
        return "test.test";
    }
}
