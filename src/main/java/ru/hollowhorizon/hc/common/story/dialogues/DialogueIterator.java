package ru.hollowhorizon.hc.common.story.dialogues;

import java.util.function.Consumer;

public class DialogueIterator {
    private HollowDialogue dialogue;

    public DialogueIterator(HollowDialogue dialogue) {
        this.dialogue = dialogue.copy();
    }

    public boolean hasNext() {
        return dialogue.getAllPhrases().size() > 0 || dialogue.getParent() != null;
    }

    public void prepareChoices(Consumer<DialogueComponent.DialogueChoiceComponent> onPrepare) {
        Object component = nextNoUpdate();

        if (component instanceof DialogueComponent.DialogueChoiceComponent) {
            onPrepare.accept((DialogueComponent.DialogueChoiceComponent) component);
        }
    }

    public boolean isChoiceNow() {
        if (dialogue.getAllPhrases().size() == 0) return false;
        Object object = dialogue.getAllPhrases().get(0);
        return object instanceof DialogueComponent.DialogueChoiceComponent;
    }

    public void processDialogueComponent(
            Consumer<DialogueComponent.DialogueTextComponent> ifText,
            Consumer<DialogueComponent.DialogueChoiceComponent> ifChoice,
            Consumer<DialogueComponent.DialogueEffectComponent> ifEffect
    ) {
        Object component = next();
        if (component instanceof DialogueComponent.DialogueTextComponent) {
            ifText.accept((DialogueComponent.DialogueTextComponent) component);
        } else if (component instanceof DialogueComponent.DialogueChoiceComponent) {
            ifChoice.accept((DialogueComponent.DialogueChoiceComponent) component);
        } else if (component instanceof DialogueComponent.DialogueEffectComponent) {
            ifEffect.accept((DialogueComponent.DialogueEffectComponent) component);
        }

        removeCurrent();
    }

    private IDialoguePart next() {
        if (dialogue.getAllPhrases().size() > 0) return dialogue.getAllPhrases().get(0);
        else {
            HollowDialogue parent = dialogue.getParent();
            if (parent != null) {
                this.dialogue = parent.copy();
                removeCurrent();
                return next();
            } else {
                return null;
            }
        }
    }

    private IDialoguePart nextNoUpdate() {
        if (dialogue.getAllPhrases().size() > 0) return dialogue.getAllPhrases().get(0);
        return null;
    }

    private void removeCurrent() {
        dialogue.getAllPhrases().remove(0);
    }

    public void makeChoice(DialogueComponent.DialogueChoiceComponent component, ChoiceTextComponent buttonChoice, String dialogueName) {
        if (buttonChoice != null) {
            this.dialogue = component.getDialogueByChoice(buttonChoice).copy();
        }
    }
}
