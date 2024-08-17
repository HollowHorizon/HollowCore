package ru.hollowhorizon.hc.common.scripting.util



import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf


inline fun <reified E> PsiElement.findParent() =
    this.parentsWithSelf.filterIsInstance<E>().firstOrNull()

fun KtElement.asKtClass(): KtElement? {
    return this.findParent<KtImportDirective>() // import x.y.?
    // package x.y.?
        ?: this.findParent<KtPackageDirective>()
        // :?
        ?: this as? KtUserType
        ?: this.parent as? KtTypeElement
        // .?
        ?: this as? KtQualifiedExpression
        ?: this.parent as? KtQualifiedExpression
        // something::?
        ?: this as? KtCallableReferenceExpression
        ?: this.parent as? KtCallableReferenceExpression
        // something.foo() with cursor in the method
        ?: this.parent?.parent as? KtQualifiedExpression
        // ?
        ?: this as? KtNameReferenceExpression
        // x ? y (infix)
        ?: this.parent as? KtBinaryExpression
        // x()
        ?: this as? KtCallExpression
        // x (constant)
        ?: this as? KtConstantExpression
}