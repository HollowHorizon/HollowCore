package ru.hollowhorizon.hc.mixin.ftbquests;

import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenu;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.common.integration.ftb.quests.HollowChapter;

import java.util.List;
import java.util.regex.Pattern;

@Mixin(ChapterPanel.ModpackButton.class)
public class ModPackButtonMixin {
    @Redirect(method = "onClicked", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/gui/quests/QuestScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/ui/ContextMenu;"), remap = false)
    private ContextMenu init(QuestScreen questScreen, List<ContextMenuItem> menu) {

        menu.add(new ContextMenuItem(new TranslationTextComponent("ftbquests.hc_addon.story_chapter"), ThemeProperties.ADD_ICON.get(), () -> {
            GuiHelper.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F);

            StringConfig config = new StringConfig(Pattern.compile("^.+$"));
            EditConfigFromStringScreen.open(config, "", "", (accepted) -> {
                questScreen.chapterPanel.questScreen.openGui();
                if (accepted && !config.value.isEmpty()) {
                    Chapter chapter = new Chapter(questScreen.chapterPanel.questScreen.file, questScreen.chapterPanel.questScreen.file.defaultChapterGroup);
                    chapter.title = config.value;
                    chapter.alwaysInvisible = true;

                    CompoundNBT extra = new CompoundNBT();
                    extra.putLong("group", 0L);

                    HollowChapter mixin = HollowJavaUtils.castDarkMagic(chapter);

                    CompoundNBT hollowData = new CompoundNBT();
                    hollowData.putBoolean("is_story_chapter", true);
                    mixin.setExtra(hollowData);

                    (new CreateObjectMessage(chapter, extra)).sendToServer();


                }
                questScreen.run();
            });
        }));

        return questScreen.openContextMenu(menu);
    }
}
