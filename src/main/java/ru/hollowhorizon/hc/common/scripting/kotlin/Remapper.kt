package ru.hollowhorizon.hc.common.scripting.kotlin

import org.jetbrains.kotlin.com.intellij.mock.MockApplication
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPoint
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions.getRootArea
import org.jetbrains.kotlin.com.intellij.pom.PomModel
import org.jetbrains.kotlin.com.intellij.psi.impl.source.codeStyle.IndentHelper
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.TreeCopyHandler
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import ru.hollowhorizon.hc.HollowCore

object Remapper {
    @JvmStatic
    fun remapProperty(
        expression: KtSimpleNameExpression
    ): KtSimpleNameExpression {
        (expression.project as MockProject).initProject()

        val caller = expression.parent.firstChild as? KtExpression ?: return expression

        HollowCore.LOGGER.info("Remapping to ZERO")
        val new = KtPsiFactory(expression.project).createExpression("field_186680_a") as KtNameReferenceExpression
        expression.replace(new)
        return new
        //val callerClass = caller.containingKtFile.importDirectives.firstOrNull {it.importedFqName!!.asString() == caller.text}?.importedFqName?.asString() ?: return
    }

    private fun MockProject.initProject() {
        val extensionPoint = "org.jetbrains.kotlin.com.intellij.treeCopyHandler"
        val extensionClassName = TreeCopyHandler::class.java.name
        for (area in arrayOf(extensionArea, getRootArea())) {
            if (!area.hasExtensionPoint(extensionPoint)) {
                area.registerExtensionPoint(extensionPoint, extensionClassName, ExtensionPoint.Kind.INTERFACE)
            }
        }

        registerService(PomModel::class.java, FormatPomModel())
        (ApplicationManager.getApplication() as MockApplication).registerService(IndentHelper::class.java, HollowIndentHelper())
    }
}