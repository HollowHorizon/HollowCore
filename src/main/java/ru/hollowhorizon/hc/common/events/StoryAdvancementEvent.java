package ru.hollowhorizon.hc.common.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class StoryAdvancementEvent extends Event {
    private final ServerPlayerEntity player;
    private final String advancement;
    private final Type type;

    public StoryAdvancementEvent(ServerPlayerEntity player, String advancement, Type type) {
        this.advancement = advancement;
        this.type = type;
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public String getAdvancement() {
        return advancement;
    }

    public Type getType() {
        return type;
    }

    public enum Type{
        GIVE,
        TAKE
    }
}
