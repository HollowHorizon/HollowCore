package ru.hollowhorizon.hc.client.kool;

import de.fabmax.kool.KoolContext;
import de.fabmax.kool.KoolSystem;
import de.fabmax.kool.pipeline.*;
import de.fabmax.kool.pipeline.backend.gl.SceneRenderPassGl;
import de.fabmax.kool.pipeline.backend.gl.ShaderManager;
import de.fabmax.kool.scene.Scene;
import de.fabmax.kool.util.RenderLoopCoroutineDispatcher;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.mixins.kool.ShaderManagerAccessor;

import java.util.List;

public class KoolHooks {
    public static void createContext(KoolContext context) {
        KoolSystem.INSTANCE.onContextCreated$kool_core(context);
    }

    public static void executeCoroutineTasks() {
        RenderLoopCoroutineDispatcher.INSTANCE.executeDispatchedTasks$kool_core();
    }

    public static void setupScene(SceneRenderPassGl scene) {
        scene.setResolveDirect$kool_core(true);
    }

    public static void resetShaders(MCKoolContext context) {
        ShaderManagerAccessor manager = JavaHacks.forceCast(context.getBackend().getShaderMgr$kool_core());
        manager.callSetBoundShader(null);
    }

    public static ShaderManager shaderManager(MCKoolContext context) {
        return context.getBackend().getShaderMgr$kool_core();
    }

    public static List<OffscreenRenderPass> renderPasses(Scene scene) {
        return scene.getSortedOffscreenPasses$kool_core();
    }

    public static OffscreenPass2dImpl getImpl(OffscreenRenderPass2d offscreenRenderPass) {
        return offscreenRenderPass.getImpl$kool_core();
    }

    public static OffscreenPassCubeImpl getImpl(OffscreenRenderPassCube offscreenRenderPassCube) {
        return offscreenRenderPassCube.getImpl$kool_core();
    }

    public static ComputePassImpl getImpl(ComputeRenderPass computeRenderPass) {
        return computeRenderPass.getImpl$kool_core();
    }
}
