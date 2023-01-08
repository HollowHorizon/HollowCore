package ru.hollowhorizon.hc.mixin;

import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerImplsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;

import java.util.Collection;

@Mixin(value = ScriptJvmCompilerImplsKt.class, remap = false)
public class ScriptJvmCompilerImplsMixin {
    private ScriptJvmCompilerImplsMixin() {
    }

    @Inject(method = "analyze", at = @At(value = "TAIL"))
    private static void compile(Collection<KtFile> sourceFiles, KotlinCoreEnvironment environment, CallbackInfoReturnable<AnalysisResult> cir) {
        sourceFiles.forEach(file -> {
            BindingContext bindingContext = cir.getReturnValue().getBindingContext();
            //file.accept(new KotlinRemapper(file, bindingContext));

            HollowCore.LOGGER.info("Script file: {}", file.getText());
        });
    }
}
