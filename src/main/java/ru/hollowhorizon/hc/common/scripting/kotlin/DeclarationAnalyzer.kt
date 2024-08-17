package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.HierarchicalScope
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.receivers.AbstractReceiverValue
import org.jetbrains.kotlin.resolve.scopes.receivers.PackageQualifier
import org.jetbrains.kotlin.resolve.scopes.utils.parentsWithSelf
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedTypeAliasDescriptor
import org.jetbrains.kotlin.types.TypeUtils
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.scripting.util.*
import ru.hollowhorizon.hc.common.scripting.util.FieldDescriptor
import ru.hollowhorizon.hc.mixins.scripting.SlicedMapImplAccessor

fun tryGetClass(accessor: SlicedMapImplAccessor, expression: PsiElement): ClassDescriptor? {
    val info = accessor.map()[expression] ?: return null

    // Проверяем для Static
    val qualifierDescriptor = info.get(BindingContext.QUALIFIER.key)?.descriptor
    if (qualifierDescriptor is ClassDescriptor) return qualifierDescriptor

    // Проверяем на тип
    val expressionType = info.get(BindingContext.EXPRESSION_TYPE_INFO.key)?.type
    if (expressionType != null) return TypeUtils.getClassDescriptor(expressionType)

    return null
}

fun tryGetPackage(accessor: SlicedMapImplAccessor, expression: PsiElement): PackageViewDescriptor? {
    val info = accessor.map()[expression] ?: return null

    val packageQualifier = info.get(BindingContext.QUALIFIER.key) as? PackageQualifier
    if (packageQualifier != null) return packageQualifier.descriptor

    return info.get(BindingContext.REFERENCE_TARGET.key) as? PackageViewDescriptor
}

val ClassDescriptor.memberDeclarations: List<DeclarationDescriptor>
    get() = unsubstitutedMemberScope.getContributedDescriptors().toList()
val ClassDescriptor.staticDeclarations: List<DeclarationDescriptor>
    get() = staticScope.getContributedDescriptors().toList()

fun DeclarationDescriptor.isValid(start: String) = when (this) {
    is ClassDescriptor, is DeserializedTypeAliasDescriptor, is ClassDeclarationDescriptor -> name.asString()
        .startsWith(start)

    is PackageViewDescriptor, is ImportDeclarationDescriptor -> name.asString().substringAfterLast('.')
        .startsWith(start) && name.asString()
        .isNotEmpty()

    is FunctionDescriptor -> name.asString()
        .startsWith(start) && (visibility == DescriptorVisibilities.PUBLIC || visibility == DescriptorVisibilities.LOCAL)

    is PropertyDescriptor -> name.asString()
        .startsWith(start) && (visibility == DescriptorVisibilities.PUBLIC || visibility == DescriptorVisibilities.LOCAL)

    is ValueParameterDescriptor -> name.asString()
        .startsWith(start) && (visibility == DescriptorVisibilities.PUBLIC || visibility == DescriptorVisibilities.LOCAL)

    is LocalVariableDescriptor -> name.asString()
        .startsWith(start) && (visibility == DescriptorVisibilities.PUBLIC || visibility == DescriptorVisibilities.LOCAL)

    is ReceiverParameterDescriptor -> name.asString().startsWith(start)

    else -> {
        HollowCore.LOGGER.warn("UnknownType: {} -> {}", this.javaClass.name, this)
        false
    }
}

// WARNING: VERY BAD CODE
// TODO: Optimize the search for packages and classes with mappings support
fun completeMembers(
    module: ModuleDescriptor,
    accessor: SlicedMapImplAccessor,
    receiverExpr: PsiElement,
): Collection<DeclarationDescriptor> {
    val descriptors = HashSet<DeclarationDescriptor>()
    val elements =
        (receiverExpr.parentsWithSelf).flatMap { it.parentsWithSelf }.filterIsInstance<KtElement>().distinct()

    for (element in elements) {
        when (element) {
            is KtDotQualifiedExpression -> {
                val left = element.firstChild
                val name = element.lastChild.text

                tryGetClass(accessor, left)?.let {
                    val isStatic = accessor.map()[left]?.get(BindingContext.QUALIFIER.key) != null
                    descriptors += if (isStatic) it.staticDeclarations.filter { it.isValid(name) }
                    else it.memberDeclarations.filter { it.isValid(name) }
                }
                tryGetPackage(accessor, left)?.let {
                    descriptors += it.memberScope.getContributedDescriptors().filter { it.isValid(name) }
                }
            }

            is KtNameReferenceExpression -> {
                if (element.parent is KtDotQualifiedExpression) continue

                accessor.map()[elements.first()]?.get(BindingContext.CALL.key)?.let {
                    val name = it.callElement.text
                    val reciever = it.explicitReceiver
                    if (reciever is AbstractReceiverValue) {
                        val decl =
                            TypeUtils.getClassDescriptor(reciever.type)?.memberDeclarations?.filter { it.isValid(name) }
                        if (decl != null) descriptors += decl
                    }
                }

                val scope = accessor.map()[element]?.get(BindingContext.LEXICAL_SCOPE.key) ?: continue
                val name = element.getReferencedName()

                val ownerDescriptor = scope.ownerDescriptor
                if (ownerDescriptor.isExtension && ownerDescriptor is FunctionDescriptor) {
                    val owner = ownerDescriptor.extensionReceiverParameter
                    if (owner != null) descriptors += owner
                }
                if (ownerDescriptor is ClassConstructorDescriptor && "this".startsWith(name)) descriptors += ownerDescriptor

                descriptors += identifiers(scope).filter { it.isValid(name) }.toList()
            }

            is KtImportDirective -> {
                val match = Regex("import ((\\w+\\.)*)[\\w*]*").matchEntire(element.text) ?: continue
                val separator = if ('.' in element.text) '.' else ' '
                val name = element.text.substringAfterLast(separator)
                val parentDot = match.groupValues[1].ifBlank { "." }
                val parent = parentDot.substring(0, parentDot.length - 1)
                val parentPackage = module.getPackage(FqName.fromSegments(parent.split('.')))
                descriptors += parentPackage.memberScope.getContributedDescriptors().filter { it.isValid(name) }
            }

            is KtPackageDirective -> {
                val match = Regex("package ((\\w+\\.)*)[\\w*]*").matchEntire(element.text) ?: continue
                val separator = if ('.' in element.text) '.' else ' '
                val name = element.text.substringAfterLast(separator)
                val parentDot = match.groupValues[1].ifBlank { "." }
                val parent = parentDot.substring(0, parentDot.length - 1)
                val parentPackage = module.getPackage(FqName.fromSegments(parent.split('.')))
                descriptors += parentPackage.memberScope.getContributedDescriptors().filter { it.isValid(name) }
            }
        }
    }

    return descriptors
}

fun identifiers(scope: LexicalScope): Sequence<DeclarationDescriptor> =
    scope.parentsWithSelf.flatMap(::scopeIdentifiers).flatMap(::explodeConstructors)

private fun scopeIdentifiers(scope: HierarchicalScope): Sequence<DeclarationDescriptor> {
    val locals = scope.getContributedDescriptors().asSequence()
    val members = implicitMembers(scope)

    return locals + members
}

private fun explodeConstructors(declaration: DeclarationDescriptor): Sequence<DeclarationDescriptor> {
    return when (declaration) {
        is ClassDescriptor -> declaration.constructors.asSequence() + declaration

        else -> sequenceOf(declaration)
    }
}

private fun implicitMembers(scope: HierarchicalScope): Sequence<DeclarationDescriptor> {
    if (scope !is LexicalScope) return emptySequence()
    val implicit = scope.implicitReceiver ?: return emptySequence()

    return implicit.type.memberScope.getContributedDescriptors().asSequence()
}

val DeclarationDescriptor.completion: CodeCompletion
    get() = when (this) {
        is PropertyDescriptor -> {
            val result = returnType?.simpleClassName ?: "???"
            FieldDescriptor(name.asString(), result)
        }

        is LocalVariableDescriptor -> {
            val result = returnType.simpleClassName
            FieldDescriptor(name.asString(), result)
        }

        is ClassConstructorDescriptor -> {
            FieldDescriptor("this", returnType.simpleClassName)
        }

        is ReceiverParameterDescriptor -> {
            FieldDescriptor("this", value.type.simpleClassName)
        }

        is ValueParameterDescriptor -> {
            val result = returnType?.simpleClassName ?: "???"
            FieldDescriptor(name.asString(), result)
        }

        is FunctionDescriptor -> {
            val result = returnType?.simpleClassName ?: "???"
            val args = valueParameters.map { it.name.asString().replace("$$", "var") + ": " + it.type.simpleClassName }
            MethodDescriptor(name.asString(), args, result)
        }

        is ClassDescriptor, is DeserializedTypeAliasDescriptor, is ClassDeclarationDescriptor -> {
            ClassCompletionDescriptor(name.asString())
        }

        is PackageViewDescriptor, is ImportDeclarationDescriptor -> {
            ImportDescriptor(name.asString())
        }

        else -> ClassCompletionDescriptor(name.asString() + "_UNKNOWN_TYPE")
    }