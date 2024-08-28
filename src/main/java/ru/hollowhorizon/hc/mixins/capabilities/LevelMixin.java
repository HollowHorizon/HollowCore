package ru.hollowhorizon.hc.mixins.capabilities;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public class LevelMixin implements ICapabilityDispatcher {
    @Unique
    private final List<CapabilityInstance> hollowCore$capabilities = new ArrayList<>();

    @NotNull
    @Override
    public List<CapabilityInstance> getCapabilities() {
        return hollowCore$capabilities;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ICapabilityDispatcherKt.initialize(this);
    }

}