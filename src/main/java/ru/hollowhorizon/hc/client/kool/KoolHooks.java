package ru.hollowhorizon.hc.client.kool;

import de.fabmax.kool.KoolContext;
import de.fabmax.kool.KoolSystem;
import de.fabmax.kool.pipeline.OffscreenRenderPass;
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl;
import de.fabmax.kool.scene.Scene;
import de.fabmax.kool.util.RenderLoopCoroutineDispatcher;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.mixins.kool.ShaderManagerAccessor;

import java.util.List;

/**
 * Bypasses internal modificators
 */
public class KoolHooks {
    public static void createContext(KoolContext context) {
        KoolSystem.INSTANCE.onContextCreated$kool_core(context);
    }

    public static void executeCoroutineTasks() {
        RenderLoopCoroutineDispatcher.INSTANCE.executeDispatchedTasks$kool_core();
    }

    public static void resetShaders(MCKoolContext context) {
        ShaderManagerAccessor manager = JavaHacks.forceCast(context.getBackend().getShaderMgr$kool_core());
        manager.callSetBoundShader(null);
    }

    public static List<OffscreenRenderPass> renderPasses(Scene scene) {
        return scene.getSortedOffscreenPasses$kool_core();
    }

    public static KoolContext getContext(RenderBackendGl backend) {
        return backend.getCtx$kool_core();
    }
}
