package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import ru.hollowhorizon.hc.common.animations.AnimationHandler;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueIterator;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

import java.util.Objects;

public class InGameDialogueHandler {
    private final DialogueIterator iterator;
    private final ServerPlayerEntity player;
    private int ticker = 0;
    private int maxTicks = 0;
    private AnimationHandler.IEndable onEnd;

    private InGameDialogueHandler(HollowDialogue dialogue, ServerPlayerEntity player) {
        this.iterator = dialogue.iterator();
        this.player = player;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void start(ServerPlayerEntity player, HollowDialogue dialogue, AnimationHandler.IEndable onEnd) {
        InGameDialogueHandler handler = new InGameDialogueHandler(dialogue, player);
        handler.onEnd = onEnd;
    }

    public static void start(ServerPlayerEntity player, HollowDialogue dialogue) {
        new InGameDialogueHandler(dialogue, player);
    }

    public static void start(ServerPlayerEntity player, String dialogueName, AnimationHandler.IEndable onEnd) {
        InGameDialogueHandler handler = new InGameDialogueHandler(GUIDialogueHandler.get(dialogueName), player);
        handler.onEnd = onEnd;
    }

    public static void start(ServerPlayerEntity player, String dialogueName) {
        new InGameDialogueHandler(GUIDialogueHandler.get(dialogueName), player);
    }

    public void updateText() {
        if (iterator.hasNext()) {
            iterator.processDialogueComponent(
                    (textComponent) -> {
                        IFormattableTextComponent component;
                        if (textComponent.getCharacterName() != null) {
                            component = new TranslationTextComponent("｢");
                            component.append(textComponent.getCharacterName());
                            component.append("｣ ");
                            component.append(textComponent.getText());
                        } else {
                            component = textComponent.getText().copy();

                            if (textComponent.getAudio() != null) {
                                String[] resource = textComponent.getAudio().split(":");
                                player.getLevel().playSound(player, player.position().x, player.position().y, player.position().z, Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(resource[0], resource[1]))), SoundCategory.MASTER, 1.0f, 1.0f);
                            }
                        }

                        player.sendMessage(component, player.getUUID());
                        maxTicks = component.getString().length() * 2;
                    },
                    (choiceComponent) -> {

                    },
                    (effectComponent) -> {
                    }
            );
        } else {
            if (onEnd != null) onEnd.onEnd();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (ticker < maxTicks) {
            ticker += 1;
        } else {
            updateText();
            ticker=0;
        }
    }
}
