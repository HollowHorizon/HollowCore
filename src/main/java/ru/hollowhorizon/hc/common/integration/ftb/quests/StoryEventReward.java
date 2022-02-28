package ru.hollowhorizon.hc.common.integration.ftb.quests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

public class StoryEventReward extends Reward {
    public String storyEventName;

    public StoryEventReward(Quest q) {
        super(q);
        storyEventName = "null";
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType() {
        return FTBQuestsHandler.STORY_EVENT_REWARD;
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
    public void claim(ServerPlayerEntity serverPlayerEntity, boolean b) {
        StoryEventStarter.start(serverPlayerEntity, storyEventName);
    }
}
