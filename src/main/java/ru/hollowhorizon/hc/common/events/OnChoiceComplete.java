package ru.hollowhorizon.hc.common.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class OnChoiceComplete extends Event {
    public String regName;
    public ServerPlayerEntity player;

    public OnChoiceComplete(String regName, ServerPlayerEntity player) {
        this.regName = regName;
        this.player = player;
    }
}
