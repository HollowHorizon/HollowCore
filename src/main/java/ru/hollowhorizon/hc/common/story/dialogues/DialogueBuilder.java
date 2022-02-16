package ru.hollowhorizon.hc.common.story.dialogues;

import java.util.ArrayList;
import java.util.List;

public class DialogueBuilder {
    private final List<IDialoguePart> dialogues = new ArrayList<>();

    private DialogueBuilder() {
    }

    public static DialogueBuilder create() {
        return new DialogueBuilder();
    }

    public DialogueBuilder add(IDialoguePart part) {
        return add(part, false, 0);
    }

    public DialogueBuilder addAutoEnd(IDialoguePart part) {
        return add(part, true, -2);
    }

    public DialogueBuilder add(IDialoguePart part, boolean autoSkip, int time) {
        if (part instanceof DialogueComponent.DialogueTextComponent) {
            ((DialogueComponent.DialogueTextComponent) part).setAutoSkip(autoSkip ? time : -1);
        }
        dialogues.add(part);
        return this;
    }

    public HollowDialogue build() {
        HollowDialogue dialogue = new HollowDialogue(dialogues);

        for (IDialoguePart component : dialogue.getAllPhrases()) {
            if (component instanceof DialogueComponent.DialogueChoiceComponent) {
                ((DialogueComponent.DialogueChoiceComponent) component).setParent(dialogue);
            }
        }
        return dialogue;
    }
}
