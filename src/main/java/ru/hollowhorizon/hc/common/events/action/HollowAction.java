package ru.hollowhorizon.hc.common.events.action;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface HollowAction {
    void process(ServerPlayerEntity player);
}
