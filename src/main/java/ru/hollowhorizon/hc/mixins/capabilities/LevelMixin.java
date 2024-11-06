package ru.hollowhorizon.hc.mixins.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.blocks.BlockEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin implements ICapabilityDispatcher {
    @Shadow public abstract BlockState getBlockState(BlockPos pos);

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

    @Inject(method = "updateNeighborsAt", at = @At("HEAD"))
    private void onUpdateNeighbors(BlockPos pos, Block block, CallbackInfo ci) {
        EventBus.post(new BlockEvent.NeighborNotify(getBlockState(pos), pos, EnumSet.allOf(Direction.class)));
    }
}
