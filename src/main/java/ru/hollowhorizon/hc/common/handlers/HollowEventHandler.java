package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.graphics.BlurHelper;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2Kt;
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater;

import static net.minecraft.client.gui.GuiComponent.blit;
import static ru.hollowhorizon.hc.HollowCore.MODID;

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
        ResourceLocation location = new ResourceLocation(stack.getItem().getDescriptionId());

        String rawPath = "item." + location.getNamespace() + "." + location.getPath() + ".hollow_desc";

        Component text = Component.literal(rawPath);
        if (!text.getString().equals(rawPath)) event.getToolTip().add(text);
    }

    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onOverlay(RenderGuiOverlayEvent.Post event) {
        if(event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            BlurHelper.registerRenderCall(() -> {
                Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation(MODID, "textures/gui/algorithmlx.png"));
                blit(event.getPoseStack(), event.getWindow().getWidth() / 2 - 250, event.getWindow().getHeight() / 2 - 500, 0, 0, 500, 1000, 500, 1000);
                //DrawHelper.drawRoundedRect(190, 370, 150, 140, 5, Color.WHITE);
                //DrawHelper.drawRect(370, 350, 100, 120, Color.WHITE);
            });
            BlurHelper.draw((int) (Mth.sin((float) Minecraft.getInstance().level.dayTime() / 1000) * 8));
        }
    }

    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        Block block = event.getLevel().getBlockState(pos).getBlock();
        if(block.equals(Blocks.BEACON)) {
            event.setCanceled(true);
            //Minecraft.getInstance().setScreen(new HTMLScreen());
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        HollowCapabilityStorageV2.INSTANCE.getProviders().stream()
                .filter(element -> element.getFirst().isInstance(event.getTarget()))
                .forEach(data -> event.getTarget()
                        .getCapability(data.getSecond().invoke().getCapability())
                        .ifPresent(cap -> HollowCapabilityV2Kt.syncEntityForPlayer((HollowCapability) cap, event.getTarget(), (ServerPlayer) event.getEntity()))
                );
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {

            for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getCapabilitiesForClass(Player.class)) {
                HollowCapability origCap = (HollowCapability) event.getOriginal().getCapability(cap).orElse(null);

                ICapabilityUpdater updater = (ICapabilityUpdater) event.getEntity();

                updater.updateCapability(cap, HollowCapabilityV2Kt.serialize(origCap));

                HollowCapabilityV2Kt.syncClient(origCap, event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = (ServerPlayer) event.getEntity();


        //update capabilities on clients
        for (Capability<?> cap : HollowCapabilityStorageV2.INSTANCE.getCapabilitiesForClass(Player.class)) {
            HollowCore.LOGGER.info("Syncing capability " + cap.getName());
            player.getCapability(cap).ifPresent(capability -> HollowCapabilityV2Kt.syncClient((HollowCapability) capability, player));
        }
    }
}