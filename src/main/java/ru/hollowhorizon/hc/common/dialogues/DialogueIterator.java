package ru.hollowhorizon.hc.common.dialogues;

import net.minecraft.entity.Entity;
import ru.hollowhorizon.hc.common.dialogues.HollowDialogue.DialogueComponent;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.DialogueChoiceToServer;

import java.util.Iterator;

public class DialogueIterator {
    private HollowDialogue dialogue;
    private Iterator<DialogueComponent<? extends Entity>> iterator;

    public DialogueIterator(HollowDialogue dialogue) {
        this.dialogue = dialogue;
        this.iterator = this.dialogue.getAllPhrases().iterator();

    }

    public DialogueComponent<? extends Entity> next() {
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public HollowDialogue.DialogueChoices getChoices() {
        return dialogue.getChoices();
    }

    public boolean completeChoice(String regName) {
        if (dialogue.getChoices() == null) return false;

        for (int i = 0; i < dialogue.getChoices().regName.length; i++) {
            NetworkHandler.sendMessageToServer(new DialogueChoiceToServer(regName));
            if (regName.equals(dialogue.getChoices().regName[i])) {
                if (dialogue.getChoices().nextDialogues.length > 0) {
                    this.dialogue = dialogue.getChoices().nextDialogues[i];
                    this.iterator = this.dialogue.getAllPhrases().iterator();
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }
}
