package ru.hollowhorizon.hc.common.story.dialogues;

public interface IHollowDialogue {
    HollowDialogue build(DialogueBuilder builder);

    String getName();
}
