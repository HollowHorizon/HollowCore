package ru.hollowhorizon.hc.common.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.common.registry.ModParticles;

public class HollowParticles {
    @OnlyIn(Dist.CLIENT)
    public static void process() {
        PlayerEntity player = Minecraft.getInstance().player;
        World level = player.level;

        for (int i = 0; i < 15; i++) {
            level.addAlwaysVisibleParticle(ModParticles.BLUE_FLAME, player.getX() + i / 100F, player.getY() + 1, player.getZ() + i / 100F, 20, 0, 0);
        }
    }
}
