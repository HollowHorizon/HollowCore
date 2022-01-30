package ru.hollowhorizon.hc.common.story.dialogues;

import net.minecraft.util.text.TranslationTextComponent;

public class ChoiceTextComponent extends TranslationTextComponent {
    private final String regName;

    public ChoiceTextComponent(String text, String regName) {
        super(text);
        this.regName = regName;
    }

    public String getRegName() {
        return regName;
    }
}
