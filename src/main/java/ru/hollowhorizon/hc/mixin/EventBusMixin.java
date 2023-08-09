package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;

@Mixin(EventBus.class)
public class EventBusMixin {
    @Inject(method = "post(Lnet/minecraftforge/eventbus/api/Event;)Z", at = @At("HEAD"), remap = false)
    private void onPost(Event event, CallbackInfoReturnable<Boolean> cir) {
        HollowCore.LOGGER.info("Posted event: {}", event);
    }
}
