package ru.hollowhorizon.hc.mixin.ftbquests;

import dev.ftb.mods.ftbquests.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.common.integration.ftb.quests.HollowChapter;
import ru.hollowhorizon.hc.common.integration.ftb.quests.QuestThemeLoader;

@Mixin(QuestScreen.class)
public class QuestScreenMixin {

    @Inject(method = "selectChapter", at = @At(value = "TAIL"), remap = false, cancellable = true)
    public void writeData(Chapter selectedChapter, CallbackInfo ci) {
        if (selectedChapter != null) {
            HollowChapter chapter = HollowJavaUtils.castDarkMagic(selectedChapter);

            if (chapter.getExtra().getBoolean("is_story_chapter")) {
                QuestThemeLoader.setTheme(QuestThemeLoader.STORY_THEME);
            } else {
                QuestThemeLoader.setTheme(QuestThemeLoader.DEFAULT_THEME);
            }
        }
    }
}
