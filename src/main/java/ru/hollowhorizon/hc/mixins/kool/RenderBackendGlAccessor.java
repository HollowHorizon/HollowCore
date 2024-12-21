package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.pipeline.OffscreenRenderPass;
import de.fabmax.kool.pipeline.StorageBuffer;
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl;
import kotlin.Pair;
import kotlin.Unit;
import kotlinx.coroutines.CompletableDeferred;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = RenderBackendGl.class, remap = false)
public interface RenderBackendGlAccessor {
    @Accessor("awaitedStorageBuffers")
    List<Pair<StorageBuffer, CompletableDeferred<Unit>>> getAwaitedStorageBuffers();

    @Invoker("readbackStorageBuffers")
    void callReadbackStorageBuffers();
    @Invoker("drawOffscreen")
    void callDrawOffscreen(OffscreenRenderPass pass);
}
