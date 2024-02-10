package ru.hollowhorizon.hc.mixin.ftbteams;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.JavaHacks;

@Mixin(value = Team.class, remap = false)
public class AbstractTeamMixin implements ICapabilityProvider {
    @Unique
    private @Nullable CapabilityDispatcher hollowcore$capabilities;
    @Unique
    private boolean hollowcore$initialized = false;


    @Unique
    protected final @Nullable CapabilityDispatcher hollowcore$getCapabilities() {
        if (!hollowcore$initialized) {
            hollowcore$capabilities = ForgeEventFactory.gatherCapabilities(JavaHacks.forceCast(Team.class), this, null);
            hollowcore$initialized = true;
        }
        return hollowcore$capabilities;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        final CapabilityDispatcher disp = hollowcore$getCapabilities();
        return disp == null ? LazyOptional.empty() : disp.getCapability(cap, side);
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"))
    private void injectOnSave(CallbackInfoReturnable<SNBTCompoundTag> cir) {
        HollowCore.LOGGER.info("saving team capabilities!");
        SNBTCompoundTag tag = cir.getReturnValue();
        var capabilities = hollowcore$getCapabilities();
        if (capabilities == null) return;
        tag.put("hc_caps", capabilities.serializeNBT());
    }

    @Inject(method = "deserializeNBT", at = @At("RETURN"))
    private void injectOnLoad(CompoundTag tag, CallbackInfo ci) {
        var capabilities = hollowcore$getCapabilities();
        if (capabilities == null) return;
        capabilities.deserializeNBT(tag.getCompound("hc_caps"));
    }
}
