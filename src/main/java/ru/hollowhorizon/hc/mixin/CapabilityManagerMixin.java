package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2Kt;

import java.util.Comparator;
import java.util.List;

import static net.minecraftforge.fml.Logging.CAPABILITIES;

@Mixin(value = CapabilityManager.class, remap = false)
public abstract class CapabilityManagerMixin {

    private CapabilityManagerMixin() {
    }

    @Shadow
    abstract <T> Capability<T> get(String realName, boolean registering);

    @Inject(method = "injectCapabilities", at = @At("TAIL"), remap = false)
    private void injectCapabilities(List<ModFileScanData> data, CallbackInfo ci) {
        HollowCapabilityV2Kt.callHook(data, this::get);
    }


}
