package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface HollowCutscene {
    void start(ServerPlayerEntity player);

    void stop();

    String getName();
}
