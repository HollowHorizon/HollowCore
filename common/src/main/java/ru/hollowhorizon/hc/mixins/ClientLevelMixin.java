package ru.hollowhorizon.hc.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
import ru.hollowhorizon.hc.client.utils.JavaHacks;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        ICapabilityDispatcherKt.syncIfNeeded(JavaHacks.forceCast(this));
    }
}
