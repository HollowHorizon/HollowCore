package ru.hollowhorizon.hc.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.hollowhorizon.hc.client.render.blocks.HollowBlockRenderManager;
import ru.hollowhorizon.hc.client.render.entities.HollowEntityManager;
import ru.hollowhorizon.hc.client.render.game.GPUMemoryManager;
import ru.hollowhorizon.hc.client.render.shader.ShaderManager;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> GPUMemoryManager.INSTANCE.initialize());
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ShaderManager.INSTANCE.initialize());
        HollowEntityManager.renderEntitiesModels();
        HollowBlockRenderManager.renderBlockModels();
    }

    @Override
    public void init() {
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().player.getCommandSenderWorld();
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
