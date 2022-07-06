package ru.hollowhorizon.hc.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.hollowhorizon.hc.client.render.blocks.HollowBlockRenderManager;
import ru.hollowhorizon.hc.client.render.entities.HollowEntityManager;
import ru.hollowhorizon.hc.client.render.game.AnimatrixShader;
import ru.hollowhorizon.hc.client.screens.HollowContainerScreen;
import ru.hollowhorizon.hc.common.registry.ModContainers;

import java.awt.*;
import java.io.InputStream;

import static ru.hollowhorizon.hc.HollowCore.MODID;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    //private static final NativeDiscovery discovery = new NativeDiscovery();
    public static AnimatrixShader SHADER;

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event) {
        HollowEntityManager.renderEntitiesModels();
        HollowBlockRenderManager.renderBlockModels();

        ScreenManager.register(ModContainers.HOLLOW_CONTAINER, HollowContainerScreen::new);

//        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\run");
//        try {
//            discovery.discover();
//            System.out.println("DISCOVER");
//            System.out.println(discovery.successfulStrategy());
//            System.out.println(discovery.discoveredPath());
//
//        } catch (UnsatisfiedLinkError e1) {
//            e1.printStackTrace();
//        }
//
    }

    private static Font getFont(String location, int size) {
        Font font;

        try {
            InputStream is = Minecraft.getInstance().getResourceManager()
                    .getResource(new ResourceLocation(MODID, "fonts/" + location)).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error! Font can't be loaded.");
            font = new Font("default", Font.PLAIN, +10);
        }

        return font;
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
