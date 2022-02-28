package ru.hollowhorizon.hc.common.integration.ftb.lib;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueBuilder;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueComponent;
import ru.hollowhorizon.hc.common.story.dialogues.IDialoguePart;

import java.util.ArrayList;
import java.util.List;

public class DialogueBuilderConfig {
    private final EditConfigScreen screen;
    private String dialogueName = "";
    private final List<IDialoguePart> dialogueParts = new ArrayList<>();

    public DialogueBuilderConfig() {
        ConfigGroup group = new ConfigGroup("hc.gui.ftbq.dialogue");
        group.addString("dialogue_name", dialogueName, input -> dialogueName = input, "name");

        group.addList("dialogue_list", dialogueParts, new DialogueComponentValue(), DialogueComponent.TEXT.create());

        screen = new EditConfigScreen(group);

        group.savedCallback = (save) -> {
            if(save) {
                DialogueBuilder builder = DialogueBuilder.create();
                dialogueParts.forEach((builder::add));
                GUIDialogueHandler.register(dialogueName, builder.build());
                HollowCore.LOGGER.info("dialogue done");
            }
            screen.closeGui();
        };
    }

    public EditConfigScreen getScreen() {
        return screen;
    }
}
