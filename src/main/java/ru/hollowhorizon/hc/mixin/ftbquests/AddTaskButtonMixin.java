package ru.hollowhorizon.hc.mixin.ftbquests;

import dev.ftb.mods.ftbquests.gui.quests.AddTaskButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.common.integration.ftb.quests.FTBQuestsHandler;
import ru.hollowhorizon.hc.common.integration.ftb.quests.HollowChapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(AddTaskButton.class)
public class AddTaskButtonMixin {
    @Shadow(remap = false)
    @Final
    public Quest quest;

    @Redirect(method = "onClicked", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"), remap = false)
    private Collection<TaskType> init(Map<ResourceLocation, TaskType> map) {
        HollowChapter chapter = HollowJavaUtils.castDarkMagic(quest.chapter);
        if (chapter.getExtra().getBoolean("is_story_chapter")) {
            List<TaskType> typeList = new ArrayList<>();
            for (TaskType type : map.values()) {
                if (type.id.equals(FTBQuestsHandler.STORY_EVENT_TASK.id)) {
                    typeList.add(type);
                }
            }
            return typeList;
        } else {
            List<TaskType> typeList = new ArrayList<>();
            for (TaskType type : map.values()) {
                if (!type.id.equals(FTBQuestsHandler.STORY_EVENT_TASK.id)) {
                    typeList.add(type);
                }
            }
            return typeList;
        }
    }
}
