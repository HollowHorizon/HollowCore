package ru.hollowhorizon.hc.common.integration.ftb.quests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.common.capabilities.HollowStoryCapability;
import ru.hollowhorizon.hc.common.registry.ModCapabilities;

public class StoryEventTask extends Task {
    public String storyEventName;
    private boolean isEventStarted = false;

    public StoryEventTask(Quest q) {
        super(q);
        storyEventName = "null";
    }

    @Override
    public TaskType getType() {
        return FTBQuestsHandler.STORY_EVENT_TASK;
    }

    @Override
    public void writeData(CompoundNBT nbt) {
        super.writeData(nbt);
        nbt.putString("story_event", storyEventName);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        storyEventName = nbt.getString("story_event");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(storyEventName);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
        super.readNetData(buffer);
        storyEventName = buffer.readUtf();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("story_event", storyEventName, input -> storyEventName = input, "");
    }

    @Override
    public IFormattableTextComponent getAltTitle() {
        return new TranslationTextComponent(storyEventName).setStyle(Style.EMPTY.withColor(TextFormatting.GREEN));
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 20;
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayerEntity player, ItemStack craftedItem) {
        if (teamData.isCompleted(this))
            return;

        boolean hasEvent = player.getCapability(ModCapabilities.STORY_CAPABILITY).orElse(new HollowStoryCapability()).hasStory(storyEventName);

        if (!hasEvent && isEventStarted) {
            teamData.setProgress(this, 1L);
        } else {
            teamData.setProgress(this, 0L);
        }

        if (hasEvent && !isEventStarted) {
            isEventStarted = true;
        }
    }

    public String formatMaxProgress() {
        return "1";
    }

    public String formatProgress(TeamData teamData, long progress) {
        return progress >= 1L ? "1" : "0";
    }

    @Override
    public long getMaxProgress() {
        return 1L;
    }
}
