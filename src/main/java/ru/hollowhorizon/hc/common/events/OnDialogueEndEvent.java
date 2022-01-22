package ru.hollowhorizon.hc.common.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class OnDialogueEndEvent extends Event {
    private final String dialogName;
    private final PlayerEntity player;

    public OnDialogueEndEvent(String dialogueName, PlayerEntity player) {
        this.dialogName = dialogueName;
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public String getDialogueName() {
        return dialogName;
    }
}
