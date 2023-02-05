package ru.hollowhorizon.hc.mixin;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.hollowhorizon.hc.common.capabilities.ICapabilityUpdater;

import javax.annotation.Nullable;

@Mixin(value = CapabilityProvider.class, remap = false)
public abstract class CapabilityProviderMixin implements ICapabilityUpdater {
    @Shadow @Nullable protected abstract CapabilityDispatcher getCapabilities();

    @Shadow private boolean valid;

    @Override
    public void updateCapability(@NotNull Capability<?> capability, @NotNull INBT newValue) {
        final CapabilityDispatcher disp = getCapabilities();
        if(valid && disp != null) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put(capability.getName(), newValue);
            disp.deserializeNBT(nbt);
        }
    }
}
