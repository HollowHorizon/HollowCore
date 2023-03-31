package ru.hollowhorizon.hc.common.handlers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.screens.UIScreen;
import ru.hollowhorizon.hc.common.animations.CutsceneStartHandler;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2Kt;
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

public class HollowEventHandler {
    @HollowConfig("enable_blur")
    public static boolean ENABLE_BLUR = true;

    public void init() {

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void tooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation location = stack.getItem().getRegistryName();
        if (location == null) return;

        String rawPath = "item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc";

        TranslationTextComponent text = new TranslationTextComponent(rawPath);
        if (!text.getString().equals(rawPath)) event.getToolTip().add(text);
    }

    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new UIScreen());
        }
    }

    @OnlyIn(Dist.CLIENT)
    //@SubscribeEvent(priority = EventPriority.LOWEST)
    public void cameraSetup(EntityViewRenderEvent.CameraSetup event) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(event.getRoll()));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(event.getPitch()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(event.getYaw() + 180.0F));
        //GlobalRenderInfo.currentFrameGlobal = matrixStack;
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        HollowCapabilityStorageV2.INSTANCE.getProviders().stream()
                .filter(element -> element.getFirst().isInstance(event.getTarget()))
                .forEach(data -> event.getTarget()
                        .getCapability(data.getSecond().invoke().getCap())
                        .ifPresent(cap -> HollowCapabilityV2Kt.syncEntityForPlayer((HollowCapability) cap, event.getTarget(), (ServerPlayerEntity) event.getPlayer()))
                );
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {

            for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getCapabilitiesForClass(PlayerEntity.class)) {
                HollowCapability origCap = (HollowCapability) event.getOriginal().getCapability(cap).orElse(null);

                ICapabilityUpdater updater = (ICapabilityUpdater) event.getPlayer();

                updater.updateCapability(cap, HollowCapabilityV2Kt.serialize(origCap));

                HollowCapabilityV2Kt.syncClient(origCap, event.getPlayer());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        CutsceneStartHandler.startUncompletedCutscene(player);

        StoryEventStarter.startAll(player);

        //update capabilities on clients
        for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getCapabilitiesForClass(PlayerEntity.class)) {
            HollowCore.LOGGER.info("Syncing capability " + cap.getName());
            player.getCapability(cap).ifPresent(capability -> HollowCapabilityV2Kt.syncClient((HollowCapability) capability, player));
        }
    }
}