package ru.hollowhorizon.hc.common.scripting.remapper

import org.cadixdev.bombe.type.signature.MethodSignature
import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.model.ClassMapping
import org.cadixdev.lorenz.model.MethodMapping
import org.jetbrains.kotlin.asJava.getRepresentativeLightMethod
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.jvm.JvmModifier
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.com.intellij.psi.util.ClassUtil
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.propertyNameByGetMethodName
import org.jetbrains.kotlin.load.java.propertyNameBySetMethodName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.resolve.descriptorUtil.overriddenTreeAsSequence
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor
import ru.hollowhorizon.hc.common.scripting.remapper.PsiUtils.getSignature
import java.util.*

internal class PsiMapper(
    private val map: MappingSet,
    private val remappedProject: Project?,
    private val file: PsiFile,
    private val bindingContext: BindingContext,
    private val patterns: PsiPatterns?
) {
    private val mixinMappings = mutableMapOf<String, ClassMapping<*, *>>()
    private val errors = mutableListOf<Pair<Int, String>>()
    private val changes = TreeMap<TextRange, String>(compareBy<TextRange> { it.startOffset }.thenBy { it.endOffset })

    private fun error(at: PsiElement, message: String) {
        val line = StringUtil.offsetToLineNumber(file.text, at.textOffset)
        errors.add(Pair(line, message))
    }

    private fun replace(e: PsiElement, with: String) = replace(e.textRange, with)
    private fun replace(textRange: TextRange, with: String) {
        changes.compute(textRange) { _, replacement ->
            if (replacement != null) {
                replacement + with
            } else {
                with
            }
        }
    }

    private fun replaceIdentifier(parent: PsiElement, with: String) {
        var child = parent.firstChild
        while (child != null) {
            if (child is PsiIdentifier
                || child is ASTNode && child.elementType == KtTokens.IDENTIFIER
            ) {
                replace(child, with)
                return
            }
            child = child.nextSibling
        }
    }

    private fun valid(e: PsiElement): Boolean = valid(e.textRange)

    private fun valid(range: TextRange): Boolean {
        val before = changes.floorKey(range) ?: TextRange.EMPTY_RANGE
        val after = changes.ceilingKey(range) ?: TextRange.EMPTY_RANGE
        return !before.intersectsStrict(range) && !after.intersectsStrict(range)
    }

    private fun getResult(text: String): Pair<String, List<Pair<Int, String>>> {
        var result = text
        for ((key, value) in changes.descendingMap()) {
            result = key.replace(result, value)
        }
        return Pair(result, errors)
    }

    private fun findPsiClass(name: String, project: Project = file.project) =
        JavaPsiFacade.getInstance(project).findClass(
            name.replace('/', '.').replace('$', '.'),
            GlobalSearchScope.allScope(project),
        )

    private fun map(expr: PsiElement, field: PsiField) {
        val fieldName = field.name
        val declaringClass = field.containingClass ?: return
        val name = declaringClass.dollarQualifiedName ?: return
        var mapping: ClassMapping<*, *>? = this.mixinMappings[declaringClass.qualifiedName ?: return]
        if (mapping == null) {
            mapping = map.findClassMapping(name)
        }
        if (mapping == null) return
        val mapped = mapping.findFieldMapping(fieldName)?.deobfuscatedName
        if (mapped == null || mapped == fieldName) return
        replaceIdentifier(expr, mapped)

        if (expr is PsiJavaCodeReferenceElement
            && !expr.isQualified // qualified access is fine
            && !isSwitchCase(expr) // referencing constants in case statements is fine
        ) {
            error(
                expr, "Implicit member reference to remapped field \"$fieldName\". " +
                        "This can cause issues if the remapped reference becomes shadowed by a local variable and is therefore forbidden. " +
                        "Use \"this.$fieldName\" instead."
            )
        }
    }

    private fun map(expr: PsiElement, method: PsiMethod) {
        if (method.isConstructor) {
            if (expr is KtSimpleNameExpression) {
                map(expr, method.containingClass ?: return)
            }
            return
        }

        val mapping = findMapping(method)
        val mapped = mapping?.deobfuscatedName
        if (mapped != null && mapped != method.name) {
            val maybeGetter = propertyNameByGetMethodName(Name.identifier(mapped))
            if (maybeGetter != null // must have getter-style name
                && !method.hasParameters() // getters cannot take any arguments
                && method.returnType != PsiType.VOID // and must return some value
                && !method.hasModifier(JvmModifier.STATIC) // synthetic properties cannot be static
                // `super.getDebugInfo()` is a special case which cannot be replaced with a synthetic property
                && expr.parent.parent.let { it !is KtDotQualifiedExpression || it.firstChild !is KtSuperExpression }
                // cannot use synthetic properties if there is a field of the same name
                && !isSyntheticPropertyShadowedByField(maybeGetter.identifier, mapping, expr)
                // cannot use synthetic properties outside of kotlin files (cause they're a kotlin thing)
                && expr.containingFile is KtFile
            ) {
                // E.g. `entity.canUsePortal()` maps to `entity.isNonBoss()` but when we're using kotlin and the target
                // isn't (as should be the case for remapped names), we can also write that as `entity.isNonBoss` (i.e.
                // as a synthetic property).
                // This is the reverse to the operation in [map(PsiElement, SyntheticJavaPropertyDescriptor)].
                replace(expr.parent, maybeGetter.identifier)
                return
            }

            val parent = expr.parent
            if (parent is KtCallExpression) {
                val argumentList = parent.valueArgumentList
                val maybeSetter = getSyntheticPropertyForSetter(expr, method, mapping)
                if (maybeSetter != null && argumentList != null) {
                    val leftParen = argumentList.leftParenthesis!!
                    val rightParen = argumentList.rightParenthesis!!
                    replace(
                        TextRange(
                            expr.startOffset,
                            leftParen.endOffset,
                        ), "$maybeSetter =" + if (leftParen.nextSibling is PsiWhiteSpace) "" else " "
                    )
                    replace(rightParen, "")
                    return
                }
            }

            replaceIdentifier(expr, mapped)
        }
    }

    private fun getSyntheticPropertyForSetter(expr: PsiElement, method: PsiMethod, mapping: MethodMapping): String? {
        // Check if the setter method qualifies for synthetic property conversion
        if (method.returnType != PsiType.VOID) return null
        if (method.hasModifier(JvmModifier.STATIC)) return null
        val parameter = method.parameterList.parameters.singleOrNull() ?: return null
        val type = ClassUtil.getBinaryPresentation(parameter.type)

        // `super.setDebugInfo(value)` is a special case which cannot be replaced with a synthetic property
        if (expr.parent.parent.let { it is KtDotQualifiedExpression && it.firstChild is KtSuperExpression }) {
            return null
        }

        val setterName = mapping.deobfuscatedName

        for (withIsPrefix in listOf(false, true)) {
            if (withIsPrefix && type != "Z") continue // only boolean types may use the `is` prefix
            val propertyName = propertyNameBySetMethodName(Name.identifier(setterName), withIsPrefix) ?: continue
            val property = propertyName.identifier

            val getterName = getterNameByPropertyName(property, withIsPrefix)
            mapping.parent.methodMappings.find {
                it.deobfuscatedName == getterName
                        && it.descriptor.paramTypes.isEmpty()
                        && it.descriptor.returnType.toString() == type
            } ?: continue

            if (isSyntheticPropertyShadowedByField(property, mapping, expr)) {
                return null
            }

            return property
        }

        return null
    }

    private fun getterNameByPropertyName(propertyName: String, withIsPrefix: Boolean): String =
        if (withIsPrefix) propertyName else "get" + propertyName.replaceFirstChar { it.uppercaseChar() }

    private fun map(expr: PsiElement, method: KtNamedFunction) {
        val psiMethod = method.getRepresentativeLightMethod()
        val mapped = findMapping(psiMethod ?: return)?.deobfuscatedName
        if (mapped != null && mapped != method.name) {
            replaceIdentifier(expr, mapped)
        }
    }

    private fun map(expr: PsiElement, property: SyntheticJavaPropertyDescriptor) {
        val assignment = findAssignment(expr)
        if (assignment != null) {
            mapSetter(expr, assignment, property)
        } else {
            mapGetter(expr, property)
        }
    }

    private fun findAssignment(expr: PsiElement): KtBinaryExpression? {
        val parent = expr.parent
        // Our parent will either be the assignment (`expr = value`) or a qualified expression (`receiver.expr = value`)
        val leftSide = if (parent is KtQualifiedExpression) {
            if (parent.selectorExpression != expr) return null // we are on the wrong side: `expr.selector = value`
            parent
        } else {
            expr
        }
        val assignment = leftSide.parent as? KtBinaryExpression ?: return null // not an assignment after all
        if (assignment.left != leftSide) return null // turns out we are on the right side of the assignment
        if (assignment.operationToken != KtTokens.EQ) return null // not actually an assignment
        return assignment
    }

    private fun FunctionDescriptor.findPsiInHierarchy() =
        overriddenTreeAsSequence(false).firstNotNullOfOrNull { it.findPsi() } as? PsiMethod

    private fun mapGetter(expr: PsiElement, property: SyntheticJavaPropertyDescriptor) {
        val getter = property.getMethod.findPsiInHierarchy() ?: return
        val mapping = findMapping(getter)
        val mappedGetter = mapping?.deobfuscatedName ?: return
        if (mappedGetter != getter.name) {
            val maybeMapped = propertyNameByGetMethodName(Name.identifier(mappedGetter))
            if (maybeMapped == null || isSyntheticPropertyShadowedByField(maybeMapped.identifier, mapping, expr)) {
                // Can happen if a method is a synthetic property in the current mapping (e.g. `isNonBoss`) but not
                // in the target mapping (e.g. `canUsePortal()`)
                // This is the reverse to the operation in [map(PsiElement, PsiMethod)].
                replaceIdentifier(expr, "$mappedGetter()")
            } else {
                val mapped = maybeMapped.identifier
                replaceIdentifier(expr, mapped)
            }
        }
    }

    private fun mapSetter(expr: PsiElement, assignment: KtBinaryExpression, property: SyntheticJavaPropertyDescriptor) {
        val getter = property.getMethod.findPsiInHierarchy() ?: return
        val setter = property.setMethod?.findPsiInHierarchy() ?: return
        val mappingGetter = findMapping(getter)
        val mappingSetter = findMapping(setter)
        val mappedGetter = mappingGetter?.deobfuscatedName // if getter is gone, we need to switch to method invocation
        val mappedSetter = mappingSetter?.deobfuscatedName ?: return
        if (mappedGetter == null || mappedSetter != setter.name) {
            val maybeMapped = if (mappedGetter != null) {
                val isBooleanField = mappingGetter.deobfuscatedDescriptor.endsWith("Z")
                val withIsPrefix = isBooleanField && mappedGetter.startsWith("is")
                val propertyByGetter = propertyNameByGetMethodName(Name.identifier(mappedGetter))
                val propertyBySetter = propertyNameBySetMethodName(Name.identifier(mappedSetter), withIsPrefix)
                if (propertyByGetter == propertyBySetter) {
                    propertyBySetter
                } else {
                    null // remapped setter does not match remapped getter, use accessor method instead
                }
            } else {
                null
            }
            if (maybeMapped == null || isSyntheticPropertyShadowedByField(
                    maybeMapped.identifier,
                    mappingSetter,
                    expr
                )
            ) {
                val op = assignment.operationReference
                replace(
                    TextRange(
                        expr.startOffset,
                        // If there is a single whitespace after the operation element, eat it, otherwise don't
                        op.endOffset + if ((op.nextSibling as? PsiWhiteSpace)?.textLength == 1) 1 else 0
                    ), "$mappedSetter("
                )
                val right = assignment.right!!
                replace(TextRange(right.endOffset, right.endOffset), ")")
            } else {
                val mapped = maybeMapped.identifier
                replaceIdentifier(expr, mapped)
            }
        }
    }

    // See caller for why this exists
    private fun map(expr: PsiElement, method: FunctionDescriptor) {
        for (overriddenDescriptor in method.overriddenDescriptors) {
            val overriddenPsi = overriddenDescriptor.findPsi()
            if (overriddenPsi != null) {
                map(expr, overriddenPsi) // found a psi element, continue as usually
            } else {
                map(expr, overriddenDescriptor) // recursion
            }
        }
    }

    private fun findMapping(method: PsiMethod): MethodMapping? {
        var declaringClass: PsiClass? = method.containingClass ?: return null
        val parentQueue = ArrayDeque<PsiClass>()
        parentQueue.offer(declaringClass)
        var mapping: ClassMapping<*, *>? = null

        var name = declaringClass!!.qualifiedName
        if (name != null) {
            // If this method is declared in a mixin class, we want to consider the hierarchy of the target as well
            mapping = mixinMappings[name]
            // but only if the method conceptually belongs to the target class
            val isShadow = method.getAnnotation(CLASS_SHADOW) != null
            val isOverwrite = method.getAnnotation(CLASS_OVERWRITE) != null
            val isOverride = method.getAnnotation(CLASS_OVERRIDE) != null
            if (mapping != null && !isShadow && !isOverwrite && !isOverride) {
                return null // otherwise, it belongs to the mixin and never gets remapped
            }
        }
        while (true) {
            if (mapping != null) {
                val mapped = mapping.findMethodMapping(getSignature(method))
                if (mapped != null) {
                    return mapped
                }
                mapping = null
            }
            while (mapping == null) {
                declaringClass = parentQueue.poll()
                if (declaringClass == null) return null

                val superClass = declaringClass.superClass
                if (superClass != null) {
                    parentQueue.offer(superClass)
                }
                for (anInterface in declaringClass.interfaces) {
                    parentQueue.offer(anInterface)
                }

                name = declaringClass.dollarQualifiedName
                if (name == null) continue
                mapping = map.findClassMapping(name)
            }
        }
    }

    private fun map(expr: PsiElement, resolved: PsiQualifiedNamedElement) {
        val name = resolved.qualifiedName ?: return
        val dollarName = (if (resolved is PsiClass) resolved.dollarQualifiedName else name) ?: return
        val mapping = map.findClassMapping(dollarName) ?: return
        var mapped = mapping.fullDeobfuscatedName
        if (mapped == dollarName) return
        mapped = mapped.replace('/', '.').replace('$', '.')

        if (expr.text == name) {
            replace(expr, mapped)
            return
        }
        val parent: PsiElement? = expr.parent
        if ((parent is KtUserType || parent is KtQualifiedExpression) && parent.text == name) {
            if (valid(parent)) {
                replace(parent, mapped)
            }
            return
        }
        // FIXME this incorrectly filters things like "Packet<?>" and doesn't filter same-name type aliases
        // if (expr.text != name.substring(name.lastIndexOf('.') + 1)) {
        //     return // type alias, will be remapped at its definition
        // }
        replaceIdentifier(expr, mapped.substring(mapped.lastIndexOf('.') + 1))
    }

    private fun map(expr: PsiElement, resolved: PsiElement?) {
        when (resolved) {
            is PsiField -> map(expr, resolved)
            is PsiMethod -> map(expr, resolved)
            is KtNamedFunction -> map(expr, resolved.getRepresentativeLightMethod())
            is PsiClass, is PsiPackage -> map(expr, resolved as PsiQualifiedNamedElement)
        }
    }

    private fun isSyntheticPropertyShadowedByField(
        propertyName: String,
        mapping: MethodMapping,
        expr: PsiElement
    ): Boolean {
        val cls = findPsiClass(mapping.parent.fullDeobfuscatedName, remappedProject ?: file.project) ?: return false
        val field = cls.findFieldByName(propertyName, true) ?: return false

        val canAccessProtected = expr.getNonStrictParentOfType<KtClassOrObject>()?.extends(
            mapping.parent.fullObfuscatedName.replace('/', '.')
        ) == true

        return field.hasModifier(JvmModifier.PUBLIC) || (canAccessProtected && field.hasModifier(JvmModifier.PROTECTED))
    }

    private fun KtClassOrObject.extends(className: String): Boolean =
        findClassDescriptor(bindingContext)
            .getAllSuperclassesWithoutAny()
            .any { it.fqNameOrNull()?.asString() == className }

    // Note: Supports only Mixins with a single target (ignores others)
    private fun getMixinTarget(annotation: PsiAnnotation): Pair<PsiClass, ClassMapping<*, *>>? {
        for (pair in annotation.parameterList.attributes) {
            val name = pair.name
            if (name == null || "value" == name) {
                val value = pair.value
                if (value !is PsiClassObjectAccessExpression) continue
                val type = value.operand
                val reference = type.innermostComponentReferenceElement ?: continue
                val psiClass = reference.resolve() as PsiClass? ?: continue
                val qualifiedName = psiClass.dollarQualifiedName ?: continue
                val mapping = map.findClassMapping(qualifiedName) ?: continue
                return Pair(psiClass, mapping)
            }
            if ("targets" == name) {
                val value = pair.value
                if (value !is PsiLiteral) continue
                val qualifiedName = value.value as? String ?: continue
                val mapping = map.findPotentialInnerClassMapping(qualifiedName) ?: continue
                val mapped = mapping.fullDeobfuscatedName?.replace('/', '.')
                if (mapped != qualifiedName) {
                    replace(value, "\"$mapped\"")
                }
                val psiClass = findPsiClass(qualifiedName) ?: continue
                return Pair(psiClass, mapping)
            }
        }
        return null
    }

    private fun remapAccessors(mapping: ClassMapping<*, *>) {
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                val accessorAnnotation = method.getAnnotation(CLASS_ACCESSOR)
                val invokerAnnotation = method.getAnnotation(CLASS_INVOKER)
                val annotation = accessorAnnotation ?: invokerAnnotation ?: return

                val methodName = method.name
                val targetByName = when {
                    methodName.startsWith("invoke") -> methodName.substring(6)
                    methodName.startsWith("is") -> methodName.substring(2)
                    methodName.startsWith("get") || methodName.startsWith("set") -> methodName.substring(3)
                    else -> null
                }?.replaceFirstChar { it.lowercase() }

                val target = annotation.parameterList.attributes.find {
                    it.name == null || it.name == "value"
                }?.literalValue ?: targetByName
                ?: throw IllegalArgumentException("Cannot determine accessor target for $method")

                val mapped = if (invokerAnnotation != null) {
                    mapping.methodMappings.find { it.obfuscatedName == target }?.deobfuscatedName
                } else {
                    mapping.findFieldMapping(target)?.deobfuscatedName
                }
                if (mapped != null && mapped != target) {
                    // Update accessor target
                    replace(
                        annotation.parameterList, if (mapped == targetByName) {
                            // Mapped name matches implied target, can just remove the explict target
                            ""
                        } else {
                            // Mapped name does not match implied target, need to set the target as annotation value
                            "(\"" + StringUtil.escapeStringCharacters(mapped) + "\")"
                        }
                    )
                }
            }
        })
    }

    private fun remapMixinInjections(targetClass: PsiClass, mapping: ClassMapping<*, *>) {
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                val methodAttrib = method.annotations.firstNotNullOfOrNull { it.findDeclaredAttributeValue("method") }
                for ((literalExpr, literalValue) in methodAttrib?.resolvedLiteralValues ?: emptyList()) {
                    val (targetName, targetDesc) = if ('(' in literalValue) {
                        MethodSignature.of(literalValue).let { it.name to it.descriptor.toString() }
                    } else {
                        literalValue to null
                    }
                    val targetMethods = targetClass.findMethodsByName(targetName, false)
                    val targetMethod = if (targetDesc != null) {
                        targetMethods.find {
                            ClassUtil.getAsmMethodSignature(it) == targetDesc
                        }
                    } else {
                        if (targetMethods.size > 1) {
                            error(
                                literalExpr, "Ambiguous mixin method \"$targetName\" may refer to any of: ${
                                    targetMethods.joinToString {
                                        "\"${it.name}${ClassUtil.getAsmMethodSignature(it)}\""
                                    }
                                }"
                            )
                        }
                        targetMethods.firstOrNull()
                    }
                    val mappedName = targetMethod?.let(::findMapping)?.deobfuscatedName ?: targetName

                    val ambiguousName = mapping.methodMappings.count { it.deobfuscatedName == mappedName } > 1
                    val mapped = mappedName + when {
                        ambiguousName && targetMethod != null ->
                            remapMethodDesc(ClassUtil.getAsmMethodSignature(targetMethod))

                        targetDesc != null -> remapMethodDesc(targetDesc)
                        else -> ""
                    }

                    if (mapped != literalValue && valid(literalExpr)) {
                        replace(literalExpr, '"'.toString() + mapped + '"'.toString())
                    }
                }
            }
        })
    }

    private fun remapInternalType(internalType: String): String =
        StringBuilder().apply { remapInternalType(internalType, this) }.toString()

    private fun remapInternalType(internalType: String, result: StringBuilder): ClassMapping<*, *>? {
        if (internalType[0] == 'L') {
            val type = internalType.substring(1, internalType.length - 1).replace('/', '.')
            val mapping = map.findClassMapping(type)
            if (mapping != null) {
                result.append('L').append(mapping.fullDeobfuscatedName).append(';')
                return mapping
            }
        }
        result.append(internalType)
        return null
    }

    private fun remapMixinTarget(target: String): String {
        return if (target.contains(':') || target.contains('(')) {
            remapFullyQualifiedMethodOrField(target)
        } else {
            if (target[0] == 'L') {
                remapInternalType(target)
            } else {
                remapInternalType("L$target;").drop(1).dropLast(1)
            }
        }
    }

    private fun remapFullyQualifiedMethodOrField(signature: String): String {
        val ownerEnd = signature.indexOf(';')
        var argsBegin = signature.indexOf('(')
        var argsEnd = signature.indexOf(')')
        val method = argsBegin != -1
        if (!method) {
            argsEnd = signature.indexOf(':')
            argsBegin = argsEnd
        }
        val owner = signature.substring(0, ownerEnd + 1)
        val name = signature.substring(ownerEnd + 1, argsBegin)
        val returnType = signature.substring(argsEnd + 1)

        val ownerPsi = findPsiClass(owner.drop(1).dropLast(1))
        val methodPsi = if (method) {
            val desc = signature.substring(argsBegin)
            ownerPsi?.findMethodsByName(name, true)?.find { ClassUtil.getAsmMethodSignature(it) == desc }
        } else {
            null
        }

        val builder = StringBuilder(signature.length + 32)
        val mapping = remapInternalType(owner, builder)
        var mapped: String? = null
        if (methodPsi != null) {
            mapped = findMapping(methodPsi)?.deobfuscatedName
        }
        if (mapped == null && mapping != null) {
            mapped = (if (method) {
                mapping.findMethodMapping(MethodSignature.of(signature.substring(ownerEnd + 1)))
            } else {
                mapping.findFieldMapping(name)
            })?.deobfuscatedName
        }
        builder.append(mapped ?: name)
        if (method) {
            builder.append('(')
            val args = signature.substring(argsBegin + 1, argsEnd)
            var i = 0
            while (i < args.length) {
                val c = args[i]
                if (c != 'L') {
                    builder.append(c)
                    i++
                    continue
                }
                val end = args.indexOf(';', i)
                val arg = args.substring(i, end + 1)
                remapInternalType(arg, builder)
                i = end
                i++
            }
            builder.append(')')
        } else {
            builder.append(':')
        }
        remapInternalType(returnType, builder)
        return builder.toString()
    }

    private fun remapMethodDesc(desc: String): String =
        remapFullyQualifiedMethodOrField("Ldummy;dummy$desc").dropWhile { it != '(' }

    private fun remapAtTargets() {
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitAnnotation(annotation: PsiAnnotation) {
                if (CLASS_AT != annotation.qualifiedName) {
                    super.visitAnnotation(annotation)
                    return
                }

                for (attribute in annotation.parameterList.attributes) {
                    if ("target" != attribute.name) continue
                    val (value, signature) = attribute.resolvedLiteralValue ?: continue
                    val newSignature = remapMixinTarget(signature)
                    if (newSignature != signature && valid(value)) {
                        replace(value, "\"$newSignature\"")
                    }
                }
            }
        })
    }

    private fun applyPatternMatch(matcher: PsiPattern.Matcher) {
        val changes = matcher.toChanges()
        if (changes.all { valid(it.first) }) {
            changes.forEach { (range, text) -> replace(range, text) }
        } else if (changes.any { it.first !in this.changes }) {
            System.err.println("Conflicting pattern changes in $file")
            System.err.println("Proposed changes:")
            changes.forEach { println("${it.first}: \"${it.second}\" (${if (valid(it.first)) "accepted" else "rejected"})") }
            System.err.println("Current changes:")
            this.changes.forEach { println("${it.key}: \"${it.value}\"") }
        }
    }

    fun remapFile(): Pair<String, List<Pair<Int, String>>> {
        if (patterns != null) {
            file.accept(object : JavaRecursiveElementVisitor() {
                override fun visitCodeBlock(block: PsiCodeBlock) {
                    patterns.find(block).forEach { applyPatternMatch(it) }
                }

                override fun visitExpression(expression: PsiExpression) {
                    patterns.find(expression).forEach { applyPatternMatch(it) }
                }
            })
        }

        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitClass(psiClass: PsiClass) {
                val annotation = psiClass.getAnnotation(CLASS_MIXIN) ?: return

                remapAtTargets()

                val (targetClass, mapping) = getMixinTarget(annotation) ?: return

                mixinMappings[psiClass.qualifiedName!!] = mapping

                if (!mapping.fieldMappings.isEmpty()) {
                    remapAccessors(mapping)
                }
                if (!mapping.methodMappings.isEmpty()) {
                    remapMixinInjections(targetClass, mapping)
                }
            }
        })

        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitField(field: PsiField) {
                if (valid(field)) {
                    map(field, field)
                }
                super.visitField(field)
            }

            override fun visitMethod(method: PsiMethod) {
                if (valid(method)) {
                    map(method, method)
                }
                super.visitMethod(method)
            }

            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (valid(reference)) {
                    map(reference, reference.resolve())
                }
                super.visitReferenceElement(reference)
            }
        })

        if (file is KtFile) {
            file.accept(object : KtTreeVisitor<Void>() {
                override fun visitNamedFunction(function: KtNamedFunction, data: Void?): Void? {
                    if (valid(function)) {
                        map(function, function)
                    }
                    return super.visitNamedFunction(function, data)
                }

                override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression, data: Void?): Void? {
                    // Dot qualified expressions such as "a.pkg.A.Inner" we want to remap back to front because the
                    // latter parts are more specific.
                    // I.e. we start with the inner class, and only if there is no mapping for that, do we try to remap
                    // the outer class.
                    expression.selectorExpression?.accept(this)
                    expression.receiverExpression.accept(this)
                    return null
                }

                override fun visitUserType(type: KtUserType, data: Void?): Void? {
                    // Same as visitDotQualifiedExpression but for typealias declarations
                    type.referenceExpression?.accept(this)
                    type.qualifier?.accept(this)
                    type.typeArgumentList?.accept(this)
                    return null
                }

                override fun visitReferenceExpression(expression: KtReferenceExpression, data: Void?): Void? {
                    if (valid(expression)) {
                        val target = bindingContext[BindingContext.REFERENCE_TARGET, expression]
                        if (target is SyntheticJavaPropertyDescriptor) {
                            map(expression, target)
                        } else if (target != null && (target as? CallableMemberDescriptor)?.kind != CallableMemberDescriptor.Kind.SYNTHESIZED) {
                            val targetPsi = target.findPsi()
                            if (targetPsi != null) {
                                map(expression, targetPsi)
                            } else if (target is FunctionDescriptor) {
                                // Appears to be the case if we're referencing an overwritten function in a previously
                                // compiled kotlin file
                                // E.g. A.f overwrites B.f overwrites C.f
                                //      C is a Minecraft class, B is a previously compiled (and already remapped) kotlin
                                //      class and we're currently in A.f trying to call `super.f()`.
                                //      `target` is a DeserializedSimpleFunctionDescriptor with no linked PSI element.
                                map(expression, target)
                            }
                        }
                    }
                    return super.visitReferenceExpression(expression, data)
                }
            }, null)
        }

        return getResult(file.text)
    }

    companion object {
        private const val CLASS_MIXIN = "org.spongepowered.asm.mixin.Mixin"
        private const val CLASS_SHADOW = "org.spongepowered.asm.mixin.Shadow"
        private const val CLASS_OVERWRITE = "org.spongepowered.asm.mixin.Overwrite"
        private const val CLASS_ACCESSOR = "org.spongepowered.asm.mixin.gen.Accessor"
        private const val CLASS_INVOKER = "org.spongepowered.asm.mixin.gen.Invoker"
        private const val CLASS_AT = "org.spongepowered.asm.mixin.injection.At"
        private const val CLASS_OVERRIDE = "java.lang.Override"

        private fun isSwitchCase(e: PsiElement): Boolean {
            if (e is PsiSwitchLabelStatement) {
                return true
            }
            val parent = e.parent
            return parent != null && isSwitchCase(parent)
        }
    }
}