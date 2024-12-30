package ru.hollowhorizon.hc.mixins.kool.support;

import de.fabmax.kool.scene.geometry.PrimitiveType;
import kotlin.Unit;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.hollowhorizon.hc.client.kool.KoolDrawerKt;
import ru.hollowhorizon.hc.client.kool.gl.KslPbrShaderLightmapKt;

import java.util.HashSet;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin {

    private static final HashSet<BlockPos> CHUNKS = new HashSet<>();

    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;<init>()V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInit(
            float x, float y, float z, ChunkBufferBuilderPack chunkBufferBuilderPack, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir,
            ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults compileResults, int i, BlockPos blockPos, BlockPos blockPos2, VisGraph visGraph, RenderChunkRegion renderChunkRegion
    ) {
        if(CHUNKS.contains(blockPos)) {
            KoolDrawerKt.setCurrentChunkMesh(null);
            return;
        }
        var mesh = KslPbrShaderLightmapKt.addTextureMeshWithLightmap(KoolDrawerKt.getGAME_SCENE().getScene(), "chunk", false, null, PrimitiveType.TRIANGLES, v -> Unit.INSTANCE);
        mesh.getTransform().translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        mesh.setShader(KoolDrawerKt.getShader());
        KoolDrawerKt.setCurrentChunkMesh(mesh);
    }
}
