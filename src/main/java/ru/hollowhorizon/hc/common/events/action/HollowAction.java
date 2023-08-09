package ru.hollowhorizon.hc.common.events.action;

import net.minecraft.server.level.ServerPlayer;
import ru.hollowhorizon.hc.common.handlers.DelayHandler;

public interface HollowAction {
    void process(ServerPlayer player);

    default void run(ServerPlayer player, int delay) {
        DelayHandler.addDelayForAction(delay, this, player);
    }
}
