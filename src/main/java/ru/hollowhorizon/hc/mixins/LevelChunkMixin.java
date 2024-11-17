package ru.hollowhorizon.hc.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.blocks.BlockEvent;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Shadow @Final
    Level level;

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z", ordinal = 0), cancellable = true)
    private void onRemove(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        var event = new BlockEvent.Remove(level, state, pos, isMoving);
        EventBus.post(event);
        if(state != event.getState() || event.isCanceled()) cir.setReturnValue(event.getState());
    }
}
