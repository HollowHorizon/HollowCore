package ru.hollowhorizon.hc.mixins.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements ICapabilityDispatcher {
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

    @Inject(method = "saveCustomOnly", at = @At("TAIL"))
    private void serializeExtra(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        ICapabilityDispatcherKt.serializeCapabilities(this, cir.getReturnValue());
    }

    @Inject(method = "saveWithoutMetadata", at = @At("TAIL"))
    private void serialize(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        ICapabilityDispatcherKt.serializeCapabilities(this, cir.getReturnValue());
    }

    @Inject(method = "loadCustomOnly", at = @At("TAIL"))
    private void deserializeExtra(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ICapabilityDispatcherKt.deserializeCapabilities(this, tag);
    }

    @Inject(method = "loadWithComponents", at = @At("TAIL"))
    private void deserialize(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ICapabilityDispatcherKt.deserializeCapabilities(this, tag);
    }

}
