package ru.hollowhorizon.hc.common.integration.ftb.lib;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import ru.hollowhorizon.hc.common.story.dialogues.ChoiceTextComponent;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueBuilder;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueChoiceListValue extends ConfigValue<Map<ChoiceTextComponent, HollowDialogue>> {
    private final List<IDialogueValue> dialogueParts = new ArrayList<>();

    public DialogueChoiceListValue() {
    }

    @Override
    public void onClicked(MouseButton mouseButton, ConfigCallback configCallback) {
        ConfigGroup group = new ConfigGroup("hc.gui.ftbq.dialogue_choices");

        group.addList("dialogue_values", dialogueParts, new DialogueChoiceValue(), new IDialogueValue() {
            @Override
            public ChoiceTextComponent getChoice() {
                return new ChoiceTextComponent("null1", "null2");
            }

            @Override
            public HollowDialogue getDialogue() {
                return DialogueBuilder.create().build();
            }
        });

        EditConfigScreen screen = new EditConfigScreen(group);

        group.savedCallback = (call) -> {
            if(call) {
                Map<ChoiceTextComponent, HollowDialogue> dialogueMap = new HashMap<>();

                for (IDialogueValue value : dialogueParts) {
                    dialogueMap.put(value.getChoice(), value.getDialogue());
                }

                value = dialogueMap;
            }
            screen.closeGui();
        };

        screen.openGui();
    }
}
