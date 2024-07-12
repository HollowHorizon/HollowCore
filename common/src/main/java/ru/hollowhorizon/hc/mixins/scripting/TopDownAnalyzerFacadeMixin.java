package ru.hollowhorizon.hc.mixins.scripting;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.script.experimental.api.CompiledScript;
import kotlin.script.experimental.api.ResultWithDiagnostics;
import kotlin.script.experimental.api.ScriptCompilationConfiguration;
import kotlin.script.experimental.api.SourceCode;
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.kotlin.library.KotlinLibrary;
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TargetEnvironment;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.scripting.compiler.plugin.dependencies.ScriptsCompilationDependencies;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptDiagnosticsMessageCollector;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerImplsKt;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.SharedScriptCompilationContext;
import org.jetbrains.kotlin.storage.StorageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.scripting.kotlin.AfterCodeAnalysisEvent;

import java.util.Collection;
import java.util.List;

@Mixin(value = ScriptJvmCompilerImplsKt.class, remap = false)
public class TopDownAnalyzerFacadeMixin {

    @Inject(
            method = "doCompileWithK2", at = @At("RETURN")
    )
    private static void afterAnalyze(SharedScriptCompilationContext context, SourceCode script, List<? extends KtFile> sourceFiles, List<ScriptsCompilationDependencies.SourceDependencies> sourceDependencies, ScriptDiagnosticsMessageCollector messageCollector, Function1<? super KtFile, ? extends ScriptCompilationConfiguration> getScriptConfiguration, CallbackInfoReturnable<ResultWithDiagnostics<KJvmCompiledScript>> cir) {
        EventBus.post(new AfterCodeAnalysisEvent(context, script, sourceFiles));
    }
}
