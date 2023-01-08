package ru.hollowhorizon.hc.common.story.dialogues

import ru.hollowhorizon.hc.api.registy.StoryObject

@StoryObject
class testKt : IHollowDialogue {
    override fun build(builder: DialogueBuilder): HollowDialogue {
        return builder.add(DialogueComponent.TEXT.create().setText("AAAA")).build()
    }

    override fun getName(): String {
        return "test--"
    }
}