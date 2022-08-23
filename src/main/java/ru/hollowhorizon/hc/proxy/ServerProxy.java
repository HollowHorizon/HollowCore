package ru.hollowhorizon.hc.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ServerProxy extends CommonProxy {

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public World getClientWorld() {
        throw new IllegalStateException("Cannot be run on the server!");
    }

    @Override
    public PlayerEntity getClientPlayer() {
        throw new IllegalStateException("Cannot be run on the server!");
    }
}
