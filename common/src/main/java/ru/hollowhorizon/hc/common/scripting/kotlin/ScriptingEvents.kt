package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.load.java.structure.JavaArrayType
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaPrimitiveType
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.SharedScriptCompilationContext
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.common.events.Event
import ru.hollowhorizon.hc.common.scripting.util.getMethodsAndVariables
import ru.hollowhorizon.hc.mixins.scripting.BindingTraceContextAccessor
import ru.hollowhorizon.hc.mixins.scripting.SlicedMapImplAccessor
import kotlin.script.experimental.api.SourceCode

val JavaType.name: String
    get() {
        return if (this is JavaClassifierType) {
            classifier?.name?.asString() ?: throw ClassNotFoundException("$this is not a JavaClassifierType")
        } else if (this is JavaPrimitiveType) {
            type?.typeName?.asString() ?: "Void"
        } else if (this is JavaArrayType) {
            "Array<${componentType.name}>"
        } else {
            "???"
        }
    }

class AfterCodeAnalysisEvent(
    val context: SharedScriptCompilationContext,
    val script: SourceCode,
    val sourceFiles: List<KtFile>,
) : Event

fun completions(module: ModuleDescriptor, trace: BindingTrace, file: KtFile, cursor: Int): List<String> {
    val elements = file.findElementAt(cursor)?.parentsWithSelf?.filterIsInstance<KtExpression>() ?: return emptyList()

    val traceAccessor: BindingTraceContextAccessor = JavaHacks.forceCast(trace)
    val mapAccessor: SlicedMapImplAccessor = JavaHacks.forceCast(traceAccessor.map())

    elements.flatMap { it.parentsWithSelf }.toSet().firstIsInstanceOrNull<KtImportDirective>()?.let {
        val match = Regex("import ((\\w+\\.)*)[\\w*]*").matchEntire(it.text) ?: return@let
        val parentDot = match.groupValues[1].ifBlank { "." }
        val parent = parentDot.substring(0, parentDot.length - 1)
        val parentPackage = module.getPackage(FqName.fromSegments(parent.split('.')))
        return parentPackage.memberScope.getContributedDescriptors().map { it.name.asString() }.filter { it.isNotEmpty() }.sorted()
    }

    var isStatic = false

    val elementType = elements.mapNotNull {
        val expression = mapAccessor.map()[it]?.get(BindingContext.EXPRESSION_TYPE_INFO.key)?.type
        if (expression != null) {
            return@mapNotNull TypeUtils.getClassDescriptor(expression)
        } else {
            isStatic = true
            mapAccessor.map()[it]?.get(BindingContext.QUALIFIER.key)?.descriptor as? ClassDescriptor
        }
    }.firstOrNull() ?: return emptyList()

    val completions = elementType.getMethodsAndVariables(isStatic && elementType.kind != ClassKind.OBJECT)

    return completions.first.map { it.toString() } + completions.second.map { it.toString() }
}