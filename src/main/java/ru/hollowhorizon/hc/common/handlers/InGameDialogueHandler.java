package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.entity.Entity;
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
import ru.hollowhorizon.hc.common.dialogues.DialogueIterator;
import ru.hollowhorizon.hc.common.dialogues.HollowDialogue;

import java.util.Objects;

public class InGameDialogueHandler {
    private final DialogueIterator iterator;
    private final ServerPlayerEntity player;
    private int ticker = 0;
    private int maxTicks = 0;
    private AnimationHandler.IEndable onEnd;

    public InGameDialogueHandler(HollowDialogue dialogue, ServerPlayerEntity player) {
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

    public void updateText() {
        if (iterator.hasNext()) {
            HollowDialogue.DialogueComponent<?> component = iterator.next();
            IFormattableTextComponent text;
            if (!component.getCharacterName().getString().equals("")) {
                text = new TranslationTextComponent("｢");
                text.append(component.getCharacterName());
                text.append("｣ ");
                text.append(component.getText());
            } else {
                text = component.getText().copy();
            }
            player.sendMessage(text, player.getUUID());

            ticker = 0;
            maxTicks = text.getString().length() * 2;

            if (component.getAudio() != null) {
                String[] resource = component.getAudio().split(":");
                player.getLevel().playSound(player, player.position().x, player.position().y, player.position().z, Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(resource[0], resource[1]))), SoundCategory.MASTER, 1.0f, 1.0f);
            }
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
        }
    }
}
