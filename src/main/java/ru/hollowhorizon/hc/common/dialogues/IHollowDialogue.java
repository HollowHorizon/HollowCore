package ru.hollowhorizon.hc.common.dialogues;

public interface IHollowDialogue {
    HollowDialogue build(DialogueBuilder builder);

    String getName();
}
