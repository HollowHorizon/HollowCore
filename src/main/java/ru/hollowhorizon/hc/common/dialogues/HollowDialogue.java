package ru.hollowhorizon.hc.common.dialogues;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HollowDialogue {
    private final List<DialogueComponent<? extends Entity>> allPhrases;
    private final DialogueChoices choices;

    public HollowDialogue(List<DialogueComponent<? extends Entity>> dialogue, DialogueChoices choice) {
        this.allPhrases = dialogue;
        this.choices = choice;
    }

    public List<DialogueComponent<? extends Entity>> getAllPhrases() {
        return allPhrases;
    }

    public DialogueChoices getChoices() {
        return choices;
    }

    public DialogueIterator iterator() {
        return new DialogueIterator(this);
    }

    public static class DialogueChoices {
        public final String[] regName;
        public final ITextComponent[] text;
        public final HollowDialogue[] nextDialogues;

        public DialogueChoices(String[] registryName, ITextComponent[] text, HollowDialogue[] nextDialogues) {
            this.regName = registryName;
            this.text = text;
            this.nextDialogues = nextDialogues;
        }
    }

    public static class DialogueComponent<T extends Entity> {
        private ITextComponent text;
        private ITextComponent characterName;
        private Supplier<T>[] characters;
        private ResourceLocation BG;
        private String audio;
        private Consumer<T>[] action;


        public DialogueComponent() {
        }

        public ITextComponent getText() {
            return text;
        }

        public DialogueComponent<T> setText(ITextComponent text) {
            this.text = text;
            return this;
        }

        public DialogueComponent<T> setText(String text) {
            this.text = new TranslationTextComponent(text);
            return this;
        }

        public ITextComponent getCharacterName() {
            if (characterName.getString().equals("%PLAYER%")) {
                characterName = new StringTextComponent(Minecraft.getInstance().player.getGameProfile().getName());
            }
            return characterName;
        }

        public DialogueComponent<T> setCharacterName(TranslationTextComponent name) {
            this.characterName = name;
            return this;
        }

        public DialogueComponent<T> setCharacterName(String name) {
            this.characterName = new TranslationTextComponent(name);
            return this;
        }

        public Entity[] getCharacters() {
            List<Entity> list = new ArrayList<>();
            for(Supplier<T> character : characters) {
                list.add(character.get());
            }
            return list.toArray(new Entity[0]);
        }

        @SafeVarargs
        public final DialogueComponent<T> setCharacterEntity(Supplier<T>... images) {

            this.characters = images;
            return this;
        }

        public ResourceLocation getBG() {
            return BG;
        }

        public DialogueComponent<T> setBG(ResourceLocation BG) {
            this.BG = BG;
            return this;
        }

        public Consumer<T>[] getAction() {
            return action;
        }

        @SafeVarargs
        public final DialogueComponent<T> setAction(Consumer<T>... action) {
            this.action = action;
            return this;
        }

        public String getAudio() {
            return audio;
        }

        public DialogueComponent<T> setAudio(String audio) {
            this.audio = audio;
            return this;
        }
    }
}
