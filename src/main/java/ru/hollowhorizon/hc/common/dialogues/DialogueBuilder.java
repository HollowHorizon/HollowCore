package ru.hollowhorizon.hc.common.dialogues;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DialogueBuilder {
    private final List<HollowDialogue.DialogueComponent<? extends Entity>> dialogues = new ArrayList<>();
    private HollowDialogue.DialogueChoices choice;

    public <T extends Entity> DialogueBuilder addComponent(HollowDialogue.DialogueComponent<T> component) {
        this.dialogues.add(component);
        return this;
    }

    public <T extends Entity> DialogueBuilder addComponent(IComponent<T> component) {
        HollowDialogue.DialogueComponent<T> componentData = new HollowDialogue.DialogueComponent<>();
        component.component(componentData);
        this.dialogues.add(componentData);
        return this;
    }

    public <T extends Entity> DialogueBuilder addComponent(String name, String text, Supplier<T>[] characterImages) {
        this.dialogues.add(new HollowDialogue.DialogueComponent<T>().setText(text).setCharacterName(name).setCharacterEntity(characterImages));
        return this;
    }

    public DialogueBuilder addChoice(HollowDialogue.DialogueChoices choice) {
        this.choice = choice;
        return this;
    }

    public HollowDialogue build() {
        return new HollowDialogue(dialogues, choice);
    }

    public interface IComponent<T extends Entity> {
        void component(HollowDialogue.DialogueComponent<T> component);
    }
}
