package ru.hollowhorizon.hc.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import ru.hollowhorizon.hc.client.models.core.BoneTownRegistry;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFSkeleton;
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel;

public class CommonProxy {
    public void init() {

    }

    public boolean isClientSide() {
        return true;
    }

    public World getClientWorld() {
        return null;
    }

    public PlayerEntity getClientPlayer() {
        return null;
    }
}