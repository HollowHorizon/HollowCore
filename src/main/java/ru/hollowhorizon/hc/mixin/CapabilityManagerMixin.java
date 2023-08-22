package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.capabilities.CapabilityLoaderKt;

import java.util.List;

@Mixin(value = CapabilityManager.class, remap = false)
public abstract class CapabilityManagerMixin {

    private CapabilityManagerMixin() {
    }

    @Shadow
    abstract <T> Capability<T> get(String realName, boolean registering);

    @Inject(method = "injectCapabilities", at = @At("TAIL"), remap = false)
    private void injectCapabilities(List<ModFileScanData> data, CallbackInfo ci) {
        CapabilityLoaderKt.callHook(data, this::get);
    }


}
