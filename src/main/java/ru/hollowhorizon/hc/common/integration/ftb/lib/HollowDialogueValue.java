package ru.hollowhorizon.hc.common.integration.ftb.lib;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueBuilder;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueComponent;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;
import ru.hollowhorizon.hc.common.story.dialogues.IDialoguePart;

import java.util.ArrayList;
import java.util.List;

public class HollowDialogueValue extends ConfigValue<HollowDialogue> {
    private final List<IDialoguePart> dialogueParts = new ArrayList<>();

    @Override
    public void onClicked(MouseButton mouseButton, ConfigCallback configCallback) {
        ConfigGroup group = new ConfigGroup("hc.gui.ftbq.dialogue");

        group.addList("dialogue_list", dialogueParts, new DialogueComponentValue(), DialogueComponent.TEXT.create());

        EditConfigScreen screen = new EditConfigScreen(group);

        group.savedCallback = (save) -> {
            if (save) {
                DialogueBuilder builder = DialogueBuilder.create();
                dialogueParts.forEach((builder::add));

                value = builder.build();
            }
            screen.closeGui();
        };

        screen.openGui();
    }
}
