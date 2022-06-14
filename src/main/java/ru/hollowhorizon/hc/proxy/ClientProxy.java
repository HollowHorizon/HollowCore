package ru.hollowhorizon.hc.proxy;

import com.sun.jna.NativeLibrary;
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
import ru.hollowhorizon.hc.client.render.game.AnimatrixShader;
import ru.hollowhorizon.hc.client.render.game.GPUMemoryManager;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    private static final NativeDiscovery discovery = new NativeDiscovery();
    public static AnimatrixShader SHADER;

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> GPUMemoryManager.INSTANCE.initialize());
//        event.enqueueWork(() -> {
//            try {
//                SHADER = new AnimatrixShader();
//                IModelLoaderManager.Holder.setup(new ModelLoaderManager());
//                IAnimationLoaderManager.Holder.setup(new AnimationLoaderManager());
//
//                IModelLoaderManager.getInstance().registerLoader(new ColladaModelLoader());
//                IAnimationLoaderManager.getInstance().registerLoader(new ColladaAnimationLoader());
//
//                try {
//                    IAnimation animation = IAnimationLoaderManager.getInstance().loadAnimation(ModModels.TEST, new ResourceLocation(MODID, "models/cyborg.dae"));
//                    ModModels.TEST.getAnimator().startAnimation(animation);
//                    System.out.println(ModModels.TEST.getSkeleton().getRootJoint() + " " + ModModels.TEST.getSkeleton().getJointCount());
//                } catch (AnimationLoadingException e) {
//                    e.printStackTrace();
//                }
//            } catch (final IOException e) {
//                HollowCore.LOGGER.error("Failed to load Animatrix.", e);
//                throw new RuntimeException("Animatrix failure during loading.");
//            }
//        });
        HollowEntityManager.renderEntitiesModels();
        HollowBlockRenderManager.renderBlockModels();

        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\run");
        try {
            discovery.discover();
            System.out.println("DISCOVER");
            System.out.println(discovery.successfulStrategy());
            System.out.println(discovery.discoveredPath());

        } catch (UnsatisfiedLinkError e1) {
            e1.printStackTrace();
        }

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
