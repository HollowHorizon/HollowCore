package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.pipeline.backend.gl.CompiledShader;
import de.fabmax.kool.pipeline.backend.gl.ShaderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShaderManager.class, remap = false)
public interface ShaderManagerAccessor {
    @Accessor("boundShader")
    CompiledShader callGetBoundShader();
    @Accessor("boundShader")
    void callSetBoundShader(CompiledShader boundShader);
}
