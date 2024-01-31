package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.screens.EntityNodePickerScreen;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage;

public class HollowEventHandler {
    @HollowConfig("enable_blur")
    public static boolean ENABLE_BLUR = true;

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onKey(ScreenEvent.KeyPressed event) {
        if (event.getKeyCode() == GLFW.GLFW_KEY_V) {
            Minecraft.getInstance().setScreen(new EntityNodePickerScreen());
        }
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

                newCap.deserializeNBT(((CapabilityInstance) origCap.orElse(null)).serializeNBT());
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