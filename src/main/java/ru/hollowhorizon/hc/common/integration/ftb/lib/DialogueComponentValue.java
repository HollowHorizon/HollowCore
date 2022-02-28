package ru.hollowhorizon.hc.common.integration.ftb.lib;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.story.dialogues.ChoiceTextComponent;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueComponent;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;
import ru.hollowhorizon.hc.common.story.dialogues.IDialoguePart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DialogueComponentValue extends ConfigValue<IDialoguePart> {
    private final List<String> mobs = new ArrayList<>();
    private Map<ChoiceTextComponent, HollowDialogue> choices = new HashMap<>();
    private String characterName;
    private String characterText;

    @Override
    public ConfigValue<IDialoguePart> init(ConfigGroup g, String i, IDialoguePart v, Consumer<IDialoguePart> c, IDialoguePart def) {
        super.init(g, i, v, c, def);
        if(v!=null) {
            if(v instanceof DialogueComponent.DialogueTextComponent) {
                DialogueComponent.DialogueTextComponent component = ((DialogueComponent.DialogueTextComponent) v);

                characterName = component.getCharacterName().getString();
                characterText = component.getCharacterName().getString();
            } else if (v instanceof DialogueComponent.DialogueChoiceComponent) {
                DialogueComponent.DialogueChoiceComponent component = ((DialogueComponent.DialogueChoiceComponent) v);

                choices = component.getChoices();
            }
        }
        return this;
    }

    @Override
    public void onClicked(MouseButton mouseButton, ConfigCallback configCallback) {
        new ComponentListScreen(this::processText, this::processChoice, this::processText).openGui();
    }

    public void processText() {
        ConfigGroup group = new ConfigGroup("component_text");

        group.addString("character_name", characterName, input -> characterName = input, "");
        group.addString("character_text", characterText, input -> characterText = input, "");
        group.addList("character_mobs", mobs, new StringConfig(), "");

        EditConfigScreen screen = new EditConfigScreen(group);

        group.savedCallback = (save) -> {
            if (save) {
                List<Supplier<LivingEntity>> entities = new ArrayList<>();

                for (String mob : mobs) {
                    entities.add(() -> {
                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putString("id", mob);
                        return (LivingEntity) EntityType.create(new CompoundNBT(), Minecraft.getInstance().level).get();
                    });
                }

                value = DialogueComponent.TEXT.create()
                        .setCharacterName(characterName)
                        .setCharacterEntities(entities.toArray(new Supplier[0]))
                        .setText(characterText);

            }
            screen.closeGui();
        };

        screen.openGui();
    }

    public void processChoice() {
        ConfigGroup group = new ConfigGroup("hc.gui.ftbq.component_choice");

        group.add("component_choice", new DialogueChoiceListValue(), choices, input -> choices = input, null);

        group.savedCallback = (save) -> {
            DialogueComponent.DialogueChoiceComponent component = DialogueComponent.CHOICE.create();
            for (Map.Entry<ChoiceTextComponent, HollowDialogue> entry : choices.entrySet()) {
                component.setChoice(entry.getKey(), entry.getValue());
            }

            value = component;

        };

        new EditConfigScreen(group).openGui();
    }

    public static class ComponentListScreen extends ButtonListBaseScreen {
        private final Runnable first;
        private final Runnable second;
        private final Runnable third;

        public ComponentListScreen(Runnable first, Runnable second, Runnable third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public void addButtons(Panel panel) {
            SimpleButton first = new SimpleButton(panel, new TranslationTextComponent("1"), Icons.ADD, (button, mouseButton) -> {
                closeGui();
                this.first.run();
            });
            SimpleButton second = new SimpleButton(panel, new TranslationTextComponent("2"), Icons.ADD, (button, mouseButton) -> {
                closeGui();
                this.second.run();
            });
            SimpleButton third = new SimpleButton(panel, new TranslationTextComponent("3"), Icons.ADD, (button, mouseButton) -> {
                closeGui();
                this.third.run();
            });

            first.setPosAndSize(-20, 2, 40, 16);
            first.setPosAndSize(-20, 20, 40, 16);
            first.setPosAndSize(-20, 38, 40, 16);

            panel.add(first);
            panel.add(second);
            panel.add(third);
        }
    }
}
