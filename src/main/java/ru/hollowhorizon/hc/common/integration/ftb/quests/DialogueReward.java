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
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.handlers.InGameDialogueHandler;

public class DialogueReward extends Reward {
    public String dialogueName;
    public boolean isGui;

    public DialogueReward(Quest q) {
        super(q);
        dialogueName = "null";
        isGui = true;
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType() {
        return FTBQuestsHandler.DIALOGUE_REWARD;
    }

    @Override
    public void writeData(CompoundNBT nbt) {
        super.writeData(nbt);
        nbt.putString("dialogue", dialogueName);
        nbt.putBoolean("is_gui", isGui);
    }

    @Override
    public void readData(CompoundNBT nbt) {
        super.readData(nbt);
        dialogueName = nbt.getString("dialogue");
        isGui = nbt.getBoolean("is_gui");
    }

    @Override
    public void writeNetData(PacketBuffer buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(dialogueName);
        buffer.writeBoolean(isGui);
    }

    @Override
    public void readNetData(PacketBuffer buffer) {
        super.readNetData(buffer);
        dialogueName = buffer.readUtf();
        isGui = buffer.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);

        config.addString("dialogue", dialogueName, input -> dialogueName = input, "");
        config.addBool("is_gui", isGui, input -> isGui = input, true);
    }

    @Override
    public void claim(ServerPlayerEntity serverPlayerEntity, boolean b) {
        if(isGui) {
            GUIDialogueHandler.start(serverPlayerEntity, dialogueName);
        } else {
            InGameDialogueHandler.start(serverPlayerEntity, dialogueName);
        }
    }
}
