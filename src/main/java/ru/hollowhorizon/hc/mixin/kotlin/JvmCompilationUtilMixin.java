package ru.hollowhorizon.hc.mixin.kotlin;

import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.JvmCompilationUtilKt;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.scripting.mappings.ASMRemapper;

@Mixin(value = JvmCompilationUtilKt.class, remap = false)
public class JvmCompilationUtilMixin {

    @Inject(method = "makeCompiledModule", at = @At("HEAD"))
    private static void makeCacheForClassLoading(GenerationState generationState, CallbackInfoReturnable<KJvmCompiledModuleInMemoryImpl> cir) {
        generationState.getFactory().asList().forEach(file -> ASMRemapper.CLASS_CACHE.put(file.getRelativePath(), file.asByteArray()));
    }

    @Inject(method = "makeCompiledModule", at = @At("TAIL"))
    private static void clearCacheForClassLoading(GenerationState generationState, CallbackInfoReturnable<KJvmCompiledModuleInMemoryImpl> cir) {
        ASMRemapper.CLASS_CACHE.clear();
    }

    @Redirect(method = "makeCompiledModule", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/backend/common/output/OutputFile;asByteArray()[B"))
    private static byte[] makeCompiledModule(OutputFile instance) {
        if (!instance.getRelativePath().endsWith(".class")) return instance.asByteArray();
        return ASMRemapper.INSTANCE.remap(instance.asByteArray());
    }
}
