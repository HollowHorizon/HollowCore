package ru.hollowhorizon.hc.common.integration.ftb.lib;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import ru.hollowhorizon.hc.common.story.dialogues.ChoiceTextComponent;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueBuilder;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

public class DialogueChoiceValue extends ConfigValue<IDialogueValue> {
    private String choiceName;
    private String choiceText;
    private HollowDialogue choice;

    @Override
    public void onClicked(MouseButton mouseButton, ConfigCallback configCallback) {
        ConfigGroup group = new ConfigGroup("hc.gui.ftbq.component_choice");

        group.addString("choice_text", choiceText, input -> choiceText = input, "");
        group.addString("choice_regname", choiceName, input -> choiceName = input, "");
        group.add("choices", new HollowDialogueValue(), choice, input -> choice = input, DialogueBuilder.create().build());

        EditConfigScreen screen = new EditConfigScreen(group);

        group.savedCallback = (call) -> {
            if (call) {
                value = new IDialogueValue() {
                    @Override
                    public ChoiceTextComponent getChoice() {
                        return new ChoiceTextComponent(choiceName, choiceText);
                    }

                    @Override
                    public HollowDialogue getDialogue() {
                        return choice;
                    }
                };
            }
            screen.closeGui();
        };

        screen.openGui();
    }
}
