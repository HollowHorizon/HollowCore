package ru.hollowhorizon.hc.common.integration.ftb.lib;

import ru.hollowhorizon.hc.common.story.dialogues.ChoiceTextComponent;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

public interface IDialogueValue {
    ChoiceTextComponent getChoice();
    HollowDialogue getDialogue();
}
