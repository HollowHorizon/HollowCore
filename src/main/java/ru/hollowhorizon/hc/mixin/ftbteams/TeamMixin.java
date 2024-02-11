package ru.hollowhorizon.hc.mixin.ftbteams;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamBase;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;

@Mixin(value = Team.class, remap = false)
public abstract class TeamMixin extends TeamBase {
    @Inject(method = "serializeNBT", at = @At("RETURN"))
    private void injectOnSave(CallbackInfoReturnable<SNBTCompoundTag> cir) {
        HollowCore.LOGGER.info("saving team capabilities!");

        CompoundTag tag = getExtraData();
        var capabilities = ((ICapabilityDispatcher) this).getCapabilities();
        tag.put("hc_caps", capabilities.serializeNBT());
    }

    @Inject(method = "deserializeNBT", at = @At("RETURN"))
    private void injectOnLoad(CompoundTag tag, CallbackInfo ci) {
        var capabilities = ((ICapabilityDispatcher) this).getCapabilities();
        capabilities.deserializeNBT(tag.getCompound("extra").getCompound("hc_caps"));
    }
}
