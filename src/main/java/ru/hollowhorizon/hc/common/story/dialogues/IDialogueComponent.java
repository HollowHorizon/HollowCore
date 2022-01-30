package ru.hollowhorizon.hc.common.story.dialogues;

public interface IDialogueComponent<T extends IDialoguePart> {
    T create();
}
