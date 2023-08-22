package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage;

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
    public void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        Block block = event.getLevel().getBlockState(pos).getBlock();
        if (block.equals(Blocks.BEACON)) {
            event.setCanceled(true);
            //Minecraft.getInstance().setScreen(new HTMLScreen());
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        CapabilityStorage.INSTANCE.getProviders().stream()
                .filter(element -> element.getFirst().isInstance(event.getTarget()))
                .forEach(data -> event.getTarget()
                        .getCapability(data.getSecond().invoke(event.getTarget()).getCapability())
                        .ifPresent(CapabilityInstance::sync)
                );
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {

            for (Capability<?> cap : CapabilityStorage.INSTANCE.getCapabilitiesForPlayer()) {
                CapabilityInstance origCap = (CapabilityInstance) event.getOriginal().getCapability(cap).orElse(null);
                CapabilityInstance newCap = (CapabilityInstance) event.getEntity().getCapability(cap).orElse(null);

                newCap.deserializeNBT(origCap.serializeNBT());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = (ServerPlayer) event.getEntity();


        //update capabilities on clients
        for (Capability<CapabilityInstance> cap : CapabilityStorage.INSTANCE.getCapabilitiesForPlayer()) {
            player.getCapability(cap).ifPresent(CapabilityInstance::sync);
        }
    }
}