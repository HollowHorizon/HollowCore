package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.container.getService
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.common.events.Event
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.scripting.util.CodeCompletion
import ru.hollowhorizon.hc.common.scripting.util.ImportDescriptor
import ru.hollowhorizon.hc.mixins.scripting.BindingTraceContextAccessor
import ru.hollowhorizon.hc.mixins.scripting.SlicedMapImplAccessor
import kotlin.script.experimental.api.SourceCode
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.SharedScriptCompilationContext as ScriptContext

class AfterCodeAnalysisEvent(val context: ScriptContext, val script: SourceCode, val sourceFiles: List<KtFile>) : Event
class CodeCompletionEvent(val source: SourceCode, val completions: List<CodeCompletion>) : Event

var currentCodeIndex = 0

@SubscribeEvent
fun onEvent(event: AfterCodeAnalysisEvent) {
    val environment = event.context.environment

    val (container, trace) = environment.createContainer(event.sourceFiles)
    val module = container.getService(ModuleDescriptor::class.java)

    container.get<LazyTopDownAnalyzer>()
        .analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, event.sourceFiles)

    val traceAccessor: BindingTraceContextAccessor = JavaHacks.forceCast(trace)
    val mapAccessor: SlicedMapImplAccessor = JavaHacks.forceCast(traceAccessor.map())

    val members =
        completeMembers(module, mapAccessor, event.sourceFiles.first().findElementAt(currentCodeIndex) ?: return)
            .map { it.completion }
            .distinctBy { it.toString() }
            .sortedBy { it is ImportDescriptor }
            .sortedBy { it.toString() }

    if (members.isNotEmpty()) CodeCompletionEvent(event.script, members).post()
}

fun KotlinCoreEnvironment.createContainer(sourcePath: Collection<KtFile>): Pair<ComponentProvider, BindingTraceContext> {
    val trace = CliBindingTrace(project)
    val container = TopDownAnalyzerFacadeForJVM.createContainer(
        project = project,
        files = sourcePath,
        trace = trace,
        configuration = configuration,
        packagePartProvider = ::createPackagePartProvider,
        declarationProviderFactory = ::FileBasedDeclarationProviderFactory
    )
    return Pair(container, trace)
}