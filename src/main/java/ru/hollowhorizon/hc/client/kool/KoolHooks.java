package ru.hollowhorizon.hc.client.kool;

import de.fabmax.kool.KoolContext;
import de.fabmax.kool.KoolSystem;
import de.fabmax.kool.pipeline.*;
import de.fabmax.kool.pipeline.backend.GpuTexture;
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl;
import de.fabmax.kool.pipeline.backend.gl.ShaderManager;
import de.fabmax.kool.util.RenderLoopCoroutineDispatcher;

/**
 * Bypasses internal modificators
 */
public class KoolHooks {
    public static void createContext(KoolContext context) {
        KoolSystem.INSTANCE.onContextCreated$kool_core(context);
    }

    public static void setScale(KoolContext context, Float scale) {
        context.setWindowScale$kool_core(scale);
    }

    public static void executeCoroutineTasks() {
        RenderLoopCoroutineDispatcher.INSTANCE.executeDispatchedTasks$kool_core();
    }

    public static void resetShaders(MCKoolContext context) {
        ShaderManager manager = context.getBackend().getShaderMgr$kool_core();
        manager.resetBoundShader();
    }

    public static KoolContext getContext(RenderBackendGl backend) {
        return backend.getCtx$kool_core();
    }

    public static OffscreenPass2dImpl impl(OffscreenPass2d pass) {
        return pass.getImpl$kool_core();
    }

    public static OffscreenPassCubeImpl impl(OffscreenPassCube pass) {
        return pass.getImpl$kool_core();
    }

    public static ComputePassImpl impl(ComputePass pass) {
        return pass.getImpl$kool_core();
    }

    public static GpuTexture getGpuTexture(Texture<?> tex) {
        return tex.getGpuTexture$kool_core();
    }
    public static void setGpuTexture(Texture<?> tex, GpuTexture gpu) {
        tex.setGpuTexture$kool_core(gpu);
    }
}
