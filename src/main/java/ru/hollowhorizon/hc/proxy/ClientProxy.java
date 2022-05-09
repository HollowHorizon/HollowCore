package ru.hollowhorizon.hc.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.AnimationLoaderManager;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.AnimationLoadingException;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.ColladaAnimationLoader;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.IAnimationLoaderManager;
import ru.hollowhorizon.hc.client.model.dae.loader.model.ModelLoaderManager;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.collada.ColladaModelLoader;
import ru.hollowhorizon.hc.client.model.dae.loader.model.collada.IModelLoaderManager;
import ru.hollowhorizon.hc.client.render.blocks.HollowBlockRenderManager;
import ru.hollowhorizon.hc.client.render.entities.HollowEntityManager;
import ru.hollowhorizon.hc.client.render.game.AnimatrixShader;
import ru.hollowhorizon.hc.client.render.game.GPUMemoryManager;
import ru.hollowhorizon.hc.client.web.WebManager;
import ru.hollowhorizon.hc.common.registry.ModModels;

import java.io.IOException;

import static ru.hollowhorizon.hc.HollowCore.MODID;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public static AnimatrixShader SHADER;

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> GPUMemoryManager.INSTANCE.initialize());
        event.enqueueWork(() -> {
            try
            {
                SHADER = new AnimatrixShader();
                IModelLoaderManager.Holder.setup(new ModelLoaderManager());
                IAnimationLoaderManager.Holder.setup(new AnimationLoaderManager());

                IModelLoaderManager.getInstance().registerLoader(new ColladaModelLoader());
                IAnimationLoaderManager.getInstance().registerLoader(new ColladaAnimationLoader());

                try {
                    IAnimation animation = IAnimationLoaderManager.getInstance().loadAnimation(ModModels.TEST, new ResourceLocation(MODID, "models/untitled.dae"));
                    ModModels.TEST.getAnimator().startAnimation(animation);
                } catch (AnimationLoadingException e) {
                    e.printStackTrace();
                }
            }
            catch (final IOException e)
            {
                HollowCore.LOGGER.error("Failed to load Animatrix.", e);
                throw new RuntimeException("Animatrix failure during loading.");
            }
        });
        HollowEntityManager.renderEntitiesModels();
        HollowBlockRenderManager.renderBlockModels();
        WebManager.init();
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
