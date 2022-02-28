package ru.hollowhorizon.hc.common.integration.ftb.quests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.util.ResourceLocation;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class FTBQuestsHandler {
    public static TaskType STORY_EVENT_TASK = TaskTypes.register(new ResourceLocation(MODID, "story_event_task"), StoryEventTask::new, () -> Icons.NOTES);
    public static RewardType STORY_EVENT_REWARD = RewardTypes.register(new ResourceLocation(MODID, "story_event_reward"), StoryEventReward::new, () -> Icons.NOTES);
    public static RewardType DIALOGUE_REWARD = RewardTypes.register(new ResourceLocation(MODID, "dialogue_reward"), DialogueReward::new, () -> Icons.CHAT);

    public static void init() {
        QuestThemeLoader.init();
    }
}
