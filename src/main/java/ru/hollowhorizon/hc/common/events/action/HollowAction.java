package ru.hollowhorizon.hc.common.events.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.common.handlers.DelayHandler;

public interface HollowAction {
    void process(ServerPlayerEntity player);

    default void run(ServerPlayerEntity player, int delay) {
        DelayHandler.addDelayForAction(delay, this, player);
    }
}
