package ru.hollowhorizon.hc.common.scripting.remapper

import org.cadixdev.bombe.type.ArrayType
import org.cadixdev.bombe.type.FieldType
import org.cadixdev.bombe.type.MethodDescriptor
import org.cadixdev.bombe.type.ObjectType
import org.cadixdev.bombe.type.Type
import org.cadixdev.bombe.type.VoidType
import org.cadixdev.bombe.type.signature.MethodSignature
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.com.intellij.psi.util.TypeConversionUtil

internal val PsiClass.dollarQualifiedName: String? get() {
    val parent = PsiTreeUtil.getParentOfType<PsiClass>(this, PsiClass::class.java) ?: return qualifiedName
    val parentName = parent.dollarQualifiedName ?: return qualifiedName
    val selfName = name ?: return qualifiedName
    return "$parentName$$selfName"
}

internal val PsiNameValuePair.resolvedLiteralValue: Pair<PsiLiteralExpression, String>?
    get () = value?.resolvedLiteralValue

private val PsiElement.resolvedLiteralValue: Pair<PsiLiteralExpression, String>? get () {
    var value: PsiElement? = this
    while (value is PsiReferenceExpression) {
        val resolved = value.resolve()
        value = when (resolved) {
            is PsiField -> resolved.initializer
            else -> resolved
        }
    }
    val literal = value as? PsiLiteralExpression ?: return null
    return Pair(literal, StringUtil.unquoteString(literal.text))
}

internal val PsiAnnotationMemberValue.resolvedLiteralValues: List<Pair<PsiLiteralExpression, String>>
    get () = when (this) {
        is PsiArrayInitializerMemberValue -> initializers.mapNotNull { it.resolvedLiteralValue }
        else -> listOfNotNull(resolvedLiteralValue)
    }

internal object PsiUtils {
    fun getSignature(method: PsiMethod): MethodSignature = MethodSignature(method.name, getDescriptor(method))

    private fun getDescriptor(method: PsiMethod): MethodDescriptor = MethodDescriptor(
        method.parameterList.parameters.map { getFieldType(it.type) },
        getType(method.returnType)
    )

    private fun getFieldType(type: PsiType?): FieldType = when (val erasedType = TypeConversionUtil.erasure(type)) {
        is PsiPrimitiveType -> FieldType.of(erasedType.kind.binaryName)
        is PsiArrayType -> {
            val array = erasedType as PsiArrayType?
            ArrayType(array!!.arrayDimensions, getFieldType(array.deepComponentType))
        }
        is PsiClassType -> {
            val resolved = erasedType.resolve() ?: throw NullPointerException("Failed to resolve type $erasedType")
            val qualifiedName = resolved.dollarQualifiedName
                ?: throw NullPointerException("Type $erasedType has no qualified name.")
            ObjectType(qualifiedName)
        }
        else -> throw IllegalArgumentException("Cannot translate type " + erasedType!!)
    }

    private fun getType(type: PsiType?): Type = if (TypeConversionUtil.isVoidType(type)) {
        VoidType.INSTANCE
    } else {
        getFieldType(type)
    }
}