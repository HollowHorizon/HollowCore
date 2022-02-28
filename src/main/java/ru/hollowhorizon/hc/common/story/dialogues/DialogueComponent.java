package ru.hollowhorizon.hc.common.story.dialogues;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import ru.hollowhorizon.hc.client.screens.DialogueScreen;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DialogueComponent {
    public static final IDialogueComponent<DialogueTextComponent> TEXT = DialogueTextComponent::new;
    public static final IDialogueComponent<DialogueChoiceComponent> CHOICE = DialogueChoiceComponent::new;
    public static final IDialogueComponent<DialogueEffectComponent> EFFECT = DialogueEffectComponent::new;

    public static class DialogueTextComponent implements IDialoguePart {
        private ITextComponent text;
        private ITextComponent characterName;
        private Supplier<LivingEntity>[] characters;
        private ResourceLocation BG;
        private String audio;
        private Consumer<DialogueScreen> action;
        private int autoSkip;
        private boolean skipButton = false;

        private DialogueTextComponent() {
        }

        public ITextComponent getText() {
            return text;
        }

        public DialogueTextComponent setText(ITextComponent text) {
            this.text = text;
            return this;
        }

        public DialogueTextComponent setText(String text) {
            this.text = new TranslationTextComponent(text);
            return this;
        }

        public ITextComponent getCharacterName() {
            return characterName;
        }

        public DialogueTextComponent setCharacterName(TranslationTextComponent name) {
            this.characterName = name;
            return this;
        }

        public DialogueTextComponent setCharacterName(String name) {
            this.characterName = new TranslationTextComponent(name);
            return this;
        }

        public LivingEntity[] getCharacters() {
            List<LivingEntity> list = new ArrayList<>();
            if(characters!=null) {
                for (Supplier<LivingEntity> character : characters) {
                    list.add(character.get());
                }
            }
            return list.toArray(new LivingEntity[0]);
        }

        public Supplier<LivingEntity>[] getRawCharacters() {
            return characters;
        }

        @SafeVarargs
        public final DialogueTextComponent setCharacterEntities(Supplier<LivingEntity>... images) {

            this.characters = images;
            return this;
        }

        public ResourceLocation getBG() {
            return BG;
        }

        public DialogueTextComponent setBG(ResourceLocation BG) {
            this.BG = BG;
            return this;
        }

        public Consumer<DialogueScreen> getAction() {
            return action;
        }

        public final DialogueTextComponent setAction(Consumer<DialogueScreen> action) {
            this.action = action;
            return this;
        }

        public String getAudio() {
            return audio;
        }

        public DialogueTextComponent setAudio(String audio) {
            this.audio = audio;
            return this;
        }

        public DialogueTextComponent setAutoSkip(int autoSkip) {
            this.autoSkip = autoSkip;
            return this;
        }

        public int getAutoSkip() {
            return autoSkip;
        }

        public DialogueTextComponent setSkipButton(boolean skipButton) {
            this.skipButton = skipButton;
            return this;
        }

        public boolean hasSkipButton() {
            return skipButton;
        }
    }

    public static class DialogueChoiceComponent implements IDialoguePart {
        private final Map<ChoiceTextComponent, HollowDialogue> choice = new HashMap<>();

        private DialogueChoiceComponent() {
        }

        public ChoiceTextComponent[] getTexts() {
            return choice.keySet().toArray(new ChoiceTextComponent[0]);
        }

        public DialogueChoiceComponent setChoice(ChoiceTextComponent text, HollowDialogue choice) {
            this.choice.put(text, choice);
            return this;
        }

        public DialogueChoiceComponent setChoices(ChoiceTextComponent[] text, HollowDialogue[] choice) {
            for(int i = 0; i < text.length; i++) {
                this.choice.put(text[i], choice[i]);
            }
            return this;
        }

        public DialogueChoiceComponent setChoice(String text, String regName, HollowDialogue choice) {
            this.choice.put(new ChoiceTextComponent(text, regName), choice);
            return this;
        }

        public DialogueChoiceComponent setParent(HollowDialogue parent) {
            for(HollowDialogue dialogue : choice.values()) {
                dialogue.setParent(parent);
            }
            return this;
        }

        public Map<ChoiceTextComponent, HollowDialogue> getChoices() {
            return choice;
        }

        public HollowDialogue getDialogueByChoice(ChoiceTextComponent component) {
            return choice.get(component);
        }
    }

    public static class DialogueEffectComponent implements IDialoguePart {
        private final List<DialogueEffects> effects = new ArrayList<>();

        private DialogueEffectComponent() {

        }

        public DialogueEffectComponent setEffect(DialogueEffects effect) {
            this.effects.add(effect);
            return this;
        }

        public List<DialogueEffects> getEffect() {
            return effects;
        }
    }

    public enum Type {
        TEXT,
        CHOICE,
        EFFECT
    }

    public enum DialogueEffects {
        SEPIA,
        EXPLOSION
    }
}
