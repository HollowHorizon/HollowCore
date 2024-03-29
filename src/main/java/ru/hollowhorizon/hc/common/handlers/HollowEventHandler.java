package ru.hollowhorizon.hc.common.handlers;

import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage;
import ru.hollowhorizon.hc.common.ui.HollowMenuKt;
import ru.hollowhorizon.hc.common.ui.WidgetKt;
import thedarkcolour.kotlinforforge.forge.ForgeKt;

public class HollowEventHandler {

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist.isClient()) {
            ForgeKt.getMOD_CONTEXT().getKEventBus().addListener(this::onClientInit);
        }
    }


    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock().equals(Blocks.BEACON) && HollowCore.DEBUG_MODE) {
            WidgetKt.main();
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClientInit(FMLClientSetupEvent event) {
        HollowMenuKt.register(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTooltip(ItemTooltipEvent event) {
        final var desc = event.getItemStack().getItem().getDescriptionId() + ".hc_desc";
        final var shift_desc = event.getItemStack().getItem().getDescriptionId() + ".hc_shift_desc";
        final var lang = Language.getInstance();

        if (lang.has(desc)) event.getToolTip().add(Component.translatable(desc));

        if (Screen.hasShiftDown() && lang.has(shift_desc)) event.getToolTip().add(Component.translatable(desc));
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
                LazyOptional<?> origCap = event.getOriginal().getCapability(cap);
                if (!origCap.isPresent()) continue;
                CapabilityInstance newCap = (CapabilityInstance) event.getEntity().getCapability(cap).orElseThrow(() -> new IllegalStateException("Capability not present!"));

                origCap.ifPresent(orig -> newCap.deserializeNBT(((CapabilityInstance) orig).serializeNBT()));
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = (ServerPlayer) event.getEntity();

        //update capabilities on clients
        for (Capability<CapabilityInstance> cap : CapabilityStorage.INSTANCE.getCapabilitiesForPlayer()) {
            player.getCapability(cap).ifPresent(CapabilityInstance::sync);
        }

        if (ModList.get().isLoaded("ftbteams")) {
            for (Capability<?> cap : CapabilityStorage.INSTANCE.getTeamCapabilities()) {
                ((ICapabilityProvider) FTBTeamsAPI.getPlayerTeam(player)).getCapability((Capability<CapabilityInstance>) cap).ifPresent(CapabilityInstance::sync);
            }
        }
    }
}