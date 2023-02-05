package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.inventory.BeaconScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.screens.DialogueOptionsScreen;
import ru.hollowhorizon.hc.client.screens.DialogueScreen;
import ru.hollowhorizon.hc.client.screens.UIScreen;
import ru.hollowhorizon.hc.common.animations.CutsceneStartHandler;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2Kt;
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater;
import ru.hollowhorizon.hc.common.capabilities.IHollowCapability;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowEventHandler {
    @HollowConfig("enable_blur")
    public static boolean ENABLE_BLUR = true;

    public static final List<String> BLUR_WHITELIST = new ArrayList<>();

    public void init() {
        BLUR_WHITELIST.add(DialogueScreen.class.getName());
        BLUR_WHITELIST.add(DialogueOptionsScreen.class.getName());

        if (HollowCore.proxy.isClientSide()) {
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlay);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlayPost);
        }

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::tooltipEvent);
    }

    private void tooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation location = stack.getItem().getRegistryName();
        if (location == null) return;
        TranslationTextComponent text = new TranslationTextComponent("item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc");
        if (!text.getString().equals("item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc")) {
            event.getToolTip().add(text);
        }
    }

    //@SubscribeEvent
    public void onOpen(GuiOpenEvent event) {
        if(event.getGui() instanceof MainMenuScreen) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new UIScreen());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) { //Пока в разработке

            for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getStorages().values()) {
                IHollowCapability origCap = (IHollowCapability) event.getOriginal().getCapability(cap).orElse(null);
                if (origCap == null) return;
                ICapabilityUpdater updater = (ICapabilityUpdater) event.getPlayer();

                updater.updateCapability(cap, HollowCapabilityV2Kt.serialize(origCap));

                HollowCapabilityV2Kt.syncClient(origCap, event.getPlayer());
            }
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        CutsceneStartHandler.startUncompletedCutscene(player);

        StoryEventStarter.startAll(player);

        //update capabilities on clients
        for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getStorages().values()) {
            player.getCapability(cap).ifPresent(capability -> HollowCapabilityV2Kt.syncClient((IHollowCapability) capability, player));
        }
    }


    @OnlyIn(Dist.CLIENT)
    private void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (Minecraft.getInstance().screen != null) {
            if (BLUR_WHITELIST.contains(Minecraft.getInstance().screen.getClass().getName()) && ENABLE_BLUR) {
                if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                    return;
                }

                event.setCanceled(true);

                if (!event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
                    return;
                }

                Minecraft.getInstance().options.setCameraType(PointOfView.FIRST_PERSON);

                Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(MODID, "shaders/blur.json"));
            }
        }
    }

    private void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {

    }
}