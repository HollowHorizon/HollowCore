package ru.hollowhorizon.hc.mixins.kool.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.kool.KoolDrawerKt;
import ru.hollowhorizon.hc.client.kool.support.KoolChunkBufferBuilderPack;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin {
    @Mutable
    @Shadow
    @Final
    ChunkBufferBuilderPack fixedBuffers;

    @Mutable
    @Shadow @Final private Queue<ChunkBufferBuilderPack> freeBuffers;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private volatile int freeBufferCount;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ClientLevel level, LevelRenderer renderer, Executor executor, boolean is64Bit, ChunkBufferBuilderPack f_, CallbackInfo ci) {
        fixedBuffers = new KoolChunkBufferBuilderPack();

        freeBuffers = Queues.newArrayDeque(List.of(fixedBuffers));
        freeBufferCount = freeBuffers.size();
    }
}
