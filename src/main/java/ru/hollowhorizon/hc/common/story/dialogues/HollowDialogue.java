package ru.hollowhorizon.hc.common.story.dialogues;

import ru.hollowhorizon.hc.client.utils.Copyable;

import java.util.ArrayList;
import java.util.List;

public class HollowDialogue implements Copyable<HollowDialogue> {
    private final List<IDialoguePart> allPhrases;
    private HollowDialogue parent;

    public HollowDialogue(List<IDialoguePart> dialogue) {
        this.allPhrases = dialogue;
    }

    public HollowDialogue(HollowDialogue original) {
        this.allPhrases = new ArrayList<>(original.allPhrases);
        this.parent = original.parent == null ? null : original.parent.copy();
    }

    public HollowDialogue getParent() {
        return parent;
    }

    public HollowDialogue setParent(HollowDialogue parent) {
        this.parent = parent;
        return this;
    }

    public List<IDialoguePart> getAllPhrases() {
        return allPhrases;
    }

    public DialogueIterator iterator() {
        return new DialogueIterator(this);
    }

    @Override
    public HollowDialogue copy() {
        return new HollowDialogue(this);
    }
}
