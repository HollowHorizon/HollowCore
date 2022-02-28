package ru.hollowhorizon.hc.mixin.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.gui.quests.AddRewardButton;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.common.integration.ftb.quests.FTBQuestsHandler;
import ru.hollowhorizon.hc.common.integration.ftb.quests.HollowChapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(AddRewardButton.class)
public abstract class AddRewardButtonMixin extends Button {
    @Shadow(remap = false)
    @Final
    private Quest quest;

    public AddRewardButtonMixin(Panel panel, ITextComponent t, Icon i) {
        super(panel, t, i);
    }

    private Collection<RewardType> getTypes() {
        HollowChapter chapter = HollowJavaUtils.castDarkMagic(quest.chapter);
        if (chapter.getExtra().getBoolean("is_story_chapter")) {
            return RewardTypes.TYPES.values();
        } else {
            List<RewardType> typeList = new ArrayList<>();
            for (RewardType type : RewardTypes.TYPES.values()) {
                if (!type.id.equals(FTBQuestsHandler.STORY_EVENT_REWARD.id)) {

                    typeList.add(type);
                }
            }
            return typeList;
        }
    }

    @Inject(method = "onClicked", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void onClicked(MouseButton button, CallbackInfo ci) {
        this.playClickSound();
        List<ContextMenuItem> contextMenu = new ArrayList<>();

        for (RewardType type : getTypes()) {
            contextMenu.add(new ContextMenuItem(type.getDisplayName(), type.getIcon(), () -> {
                this.playClickSound();
                type.getGuiProvider().openCreationGui(this, this.quest, (reward) -> {
                    CompoundNBT extra = new CompoundNBT();
                    extra.putString("type", type.getTypeForNBT());
                    HollowChapter chapter = HollowJavaUtils.castDarkMagic(quest.chapter);
                    if (chapter.getExtra().getBoolean("is_story_chapter")) {
                        reward.autoclaim = RewardAutoClaim.INVISIBLE;
                    }
                    (new CreateObjectMessage(reward, extra)).sendToServer();
                });
            }));
        }

        this.getGui().openContextMenu(contextMenu);

        ci.cancel();
    }
}
