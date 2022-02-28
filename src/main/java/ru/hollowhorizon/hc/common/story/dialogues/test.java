package ru.hollowhorizon.hc.common.story.dialogues;

import ru.hollowhorizon.hc.api.registy.StoryObject;

@StoryObject
public class test implements IHollowDialogue {

    @Override
    public HollowDialogue build(DialogueBuilder builder) {
        return builder
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("О, похоже эта штука работает...")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("Такс, ну похоже, что ты зашёл в мир и без вылетов.")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("Это хорошо :D")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("Ну, собственно говоря для начала немного расскажу о сюжете и как он устроен, чтобы потом не было недопониманий")
                        .setSkipButton(true)
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("В различных сюжетных локациях ты найдёшь вот такие обелиски")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("После смерти в сюжетных заданиях в большинстве случаев ты окажешься у последнего активиранного обелиска, а также")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("используя эти обелиски можно будет возвращаться к одному из \"слотов\" сохранения. Но есть один нюанс:")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("эти обелиски сохраняют только сюжет и выданные награды из сюжетных заданий, а сам мир и вещи он не сохраняет")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("сам подумай, мало того, что это сложно реализуется, так и мир после нескольких дней игры в среднем весит около 1-2 гигабайт")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("если использовать все 5 слотов, то в сумме около 7 гигабайт, а теперь прибавь сюда резервные копии каждые пару часов)")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("и количество таких копий в среднем около 10 и не думаю, что это хорошая идея, если сборка будет весить 70+ гигабайт))")
                )
                .add(DialogueComponent.TEXT.create()
                        .setCharacterName("MTR")
                        .setText("Так, ну пока на этом у меня всё, хорошей игры!")
                )
                .build();
    }

    @Override
    public String getName() {
        return "test++";
    }
}
