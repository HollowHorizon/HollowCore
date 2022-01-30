package ru.hollowhorizon.hc.common.story.dialogues;

import ru.hollowhorizon.hc.api.registy.StoryObject;

@StoryObject
public class test implements IHollowDialogue {
    @Override
    public HollowDialogue build(DialogueBuilder builder) {
        return builder
                .add(DialogueComponent.TEXT.create()
                        .setText("Хм...")
                )
                .add(DialogueComponent.CHOICE.create()
                        .setChoice("Хочу питсу", "want_pizza", DialogueBuilder.create()
                                .add(DialogueComponent.TEXT.create().setText("Ладно"))
                                .build())
                        .setChoice("Не хочу питсу", "no_want_pizza", DialogueBuilder.create()
                                .add(DialogueComponent.TEXT.create().setText("Почему?"))
                                .build())
                        .setChoice("я рыба", "i_fish", DialogueBuilder.create()
                                .add(DialogueComponent.TEXT.create().setText("А я пиво!"))
                                .build())
                        .setChoice("я пиво", "i_beer", DialogueBuilder.create()
                                .add(DialogueComponent.TEXT.create().setText("А я рыба!"))
                                .build())
                )
                .addAutoEnd(DialogueComponent.TEXT.create()
                        .setText("Тестирую эффекты...")
                )
                .add(DialogueComponent.EFFECT.create()
                        .setEffect(DialogueComponent.DialogueEffects.SEPIA)
                )
                .build();
    }

    @Override
    public String getName() {
        return "test++";
    }
}
